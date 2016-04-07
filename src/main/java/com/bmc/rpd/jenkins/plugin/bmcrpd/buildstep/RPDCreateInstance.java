package com.bmc.rpd.jenkins.plugin.bmcrpd.buildstep;
import com.bmc.rpd.jenkins.plugin.bmcrpd.RpdServerImpl;
import com.bmc.rpd.jenkins.plugin.bmcrpd.configuration.RPDPluginConfiguration;
import com.bmc.rpd.jenkins.plugin.bmcrpd.xml.RPDXmlResponse;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Executor;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import javax.servlet.ServletException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aivani on 1/27/2016.
 */

public class RPDCreateInstance extends Builder {
    private String customProfile;
    private String pack;
    private String instanceName;
    private Map<String, String> envMap = null;

    @DataBoundConstructor
    public RPDCreateInstance(String pack, String instanceName, String customProfile ) {
        this.pack = pack;
        this.instanceName = instanceName;
        this.customProfile = customProfile;
    }
    public RPDCreateInstance(){
        System.out.println("Instance empty constructor");
    }

    public String getPack() {return pack;}
    public  String getInstanceName() {return instanceName;}
    public String getCustomProfile() {
        if(customProfile != null && !customProfile.isEmpty())
            return customProfile;
        else
            return config.getDefaultServerProfile().getServerProfileName();
    }


    public RPDPluginConfiguration config = RPDPluginConfiguration.get();


    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            load();
        }

        @javax.inject.Inject
        RPDPluginConfiguration config = new RPDPluginConfiguration();
        public String getServerURL(){
            return config.getServerURL();
        }
        public String getUserToken(){
            return config.getUserToken();
        }
        public String generateUniqId(){
            return new BigInteger(130, new SecureRandom()).toString(32);
        }


        public FormValidation doCheckPack(@QueryParameter String pack) throws IOException, ServletException{
            if (pack.length() == 0) {
                return FormValidation.error("Package name is required.");
            }
            return FormValidation.ok();
        }
        @JavaScriptMethod
        public AutoCompletionCandidates doCompletePack(@QueryParameter String value, @QueryParameter String profile) throws IOException, ServletException{
            RPDPluginConfiguration currentProfile = config.getServerProfile(profile);
            try {
                AutoCompletionCandidates options;
                options = new AutoCompletionCandidates();
                RpdServerImpl serverInstance = new RpdServerImpl(currentProfile.getServerProfileName(), currentProfile.getServerURL(), currentProfile.getUserToken() );
                List<RPDXmlResponse> packages = serverInstance.packageListWithPartial(value);
                for (RPDXmlResponse candidate : packages)
                    options.add(candidate.getValue());
                return options;
            }
            catch (Exception e){
                return new AutoCompletionCandidates();
            }
        }

        public ListBoxModel doFillCustomProfileItems() {

            ListBoxModel items = new ListBoxModel();
            String defaultServer = "";
            if(config.getDefaultServerProfile() != null){
                defaultServer = config.getDefaultServerProfile().getServerProfileName();
                items.add(defaultServer, defaultServer);
            }
            for (RPDPluginConfiguration serverProfile : config.serverProfiles) {
                if(!defaultServer.isEmpty() && !defaultServer.equals(serverProfile.getServerProfileName())) {
                    items.add(serverProfile.getServerProfileName(), serverProfile.getServerProfileName());
                }
            }
            return items;
        }

        public FormValidation doTestConnection(@QueryParameter("rpd.serverProfileName") final String serverProfileName, @QueryParameter("rpd.serverURL") final String serverURL,
                                               @QueryParameter("rpd.userToken") final String userToken)
                throws IOException, ServletException {

            return config.doTestConnection(serverProfileName, serverURL, userToken);

        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            System.out.println(json);
            config.configure(req, json.getJSONObject("serverProfile"));
            return true;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "RPD Create Instance";
        }

        @Override
        public RPDCreateInstance newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new RPDCreateInstance(formData.getString("pack"), formData.getString("instanceName"), formData.getString("customProfile"));
        }

    }
    public RpdServerImpl getCurrentProfile(){
        RpdServerImpl result;
        RPDPluginConfiguration currentProfile = config.getServerProfile(getCustomProfile());
        return new RpdServerImpl(currentProfile.getServerProfileName(), currentProfile.getServerURL(), currentProfile.getUserToken());
    }


    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        RpdServerImpl serverProfile = getCurrentProfile();
        envMap = build.getEnvironment(listener);
        try {
            if (serverProfile == null) {
                listener.error("RPD Server URL or User Token is not specified or incorrect. Please check your configuration");
                return false;
            }
            String packageInstanceId = createPackageInstance(serverProfile, listener, envMap);
            build.addAction(new RPDVarInjectAction("RPD_"+getPack().replaceAll(" ","_").replaceAll("-","_")+"_instance_name", serverProfile.resolveVariables(getInstanceName(), envMap)));
            build.addAction(new RPDVarInjectAction("RPD_"+getPack().replaceAll(" ","_").replaceAll("-","_")+"_instance_id", packageInstanceId));
        } catch (Exception e) {
            listener.error("Failed to create RPD instance " + e.getMessage());
            return false;
        }

        return true;
    }

    class RPDVarInjectAction implements EnvironmentContributingAction {

        private String key;
        private String value;

        public RPDVarInjectAction(String key, String value) {
            this.key = key;
            this.value = value;
        }
        @Override
        public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars envVars) {
            if (envVars != null && key != null && value != null) {
                envVars.put(key, value);
            }
        }

        public String getDisplayName() {
            return "RPDVarInjectAction";
        }

        public String getIconFileName() {
            return null;
        }

        public String getUrlName() {
            return null;
        }
    }


    private String createPackageInstance(RpdServerImpl serverProfile, BuildListener listener,  Map<String, String> envMap) throws Exception {

        if (getPack() == null || getPack().trim().length() == 0) {
            System.out.println("Package is null");
            throw new Exception("The RPD Package is a required field!");

        }

        try {
            RPDXmlResponse rpdXmlResponse = serverProfile.executePackageInstanceCreate(getPack(), serverProfile.resolveVariables(getInstanceName(), envMap));
            System.out.println("After package Create");
            listener.getLogger().println("Starting creation of instance " + rpdXmlResponse.getValue() + ".  Instance Id: " + rpdXmlResponse.getId());
            String status = checkInstanceCreateStatus(serverProfile, rpdXmlResponse.getId(), listener);
            return rpdXmlResponse.getId();
        } catch (Exception e){
            listener.error(e.getMessage());
            throw new Exception(e.getMessage());
        }


    }

    private String checkInstanceCreateStatus(RpdServerImpl serverProfile, String packageInstanceId, BuildListener listener) throws Exception {

        String instanceStatus = "";

        do {
            Thread.sleep(5000);



            instanceStatus = serverProfile.executeInstanceCreateStatus(packageInstanceId);

        } while (!instanceStatus.equalsIgnoreCase("Ready") && !instanceStatus.equalsIgnoreCase("Error"));

        List<RPDXmlResponse> result = serverProfile.executeInstanceLog(packageInstanceId);
        for (RPDXmlResponse message : result) {
            listener.getLogger().println("INFO: "+message.getValue());
        }
        if (instanceStatus.equalsIgnoreCase("Error")) {

            throw new Exception("Failure: RPD Instance Creation has a status of " + instanceStatus);

        }

        listener.getLogger().println("RPD Instance Creation has a status of " + instanceStatus);

        return instanceStatus;

    }

}
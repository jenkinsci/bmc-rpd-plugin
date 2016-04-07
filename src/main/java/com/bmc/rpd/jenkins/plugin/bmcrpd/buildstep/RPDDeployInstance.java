package com.bmc.rpd.jenkins.plugin.bmcrpd.buildstep;

import com.bmc.rpd.jenkins.plugin.bmcrpd.RpdServerImpl;
import com.bmc.rpd.jenkins.plugin.bmcrpd.configuration.RPDPluginConfiguration;
import com.bmc.rpd.jenkins.plugin.bmcrpd.xml.RPDXmlResponse;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.AutoCompletionCandidates;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import javax.servlet.ServletException;
import java.awt.*;
import java.awt.dnd.Autoscroll;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aivani on 1/27/2016.
 */
public class RPDDeployInstance extends Builder {
    private final String instanceName;
    private final String pack;
    private final String environment;
    private final String route;
    private final String customProfile;
    private final Boolean useCustomProfile;
    private Map<String, String> envMap = null;

    @DataBoundConstructor
    public RPDDeployInstance(String instanceName, String pack, String environment, String route, String customProfile, Boolean useCustomProfile) {
        this.instanceName = instanceName;
        this.pack = pack;
        this.environment = environment;
        this.route = route;
        this.customProfile = customProfile;
        this.useCustomProfile = useCustomProfile;
    }

    public String getInstanceName() {return instanceName;}
    public String getPack() {return pack;}
    public String getEnvironment() {return environment;}
    public String getRoute() {return route;}
    public String getCustomProfile() {
        if(customProfile != null && !customProfile.isEmpty())
            return customProfile;
        else
            return config.getDefaultServerProfile().getServerProfileName();
    }
    public Boolean isUseCustomProfile() {return useCustomProfile;}
    public RPDPluginConfiguration config = RPDPluginConfiguration.get();

    @Extension
    public static class DeployStepDescriptor extends BuildStepDescriptor<Builder> {

        public DeployStepDescriptor() {
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

        public ListBoxModel doFillRouteItems(@QueryParameter String route, @QueryParameter String customProfile) {
            RPDPluginConfiguration currentProfile = config.getServerProfile(customProfile);
            ListBoxModel items;
            items = new ListBoxModel();
            items.add("", "");
            try {
                RpdServerImpl serverInstance = new RpdServerImpl(currentProfile.getServerProfileName(), currentProfile.getServerURL(), currentProfile.getUserToken() );

                List<RPDXmlResponse> routes = serverInstance.executeRouteList();
                for (RPDXmlResponse rpdXmlResponse : routes) {
                    items.add(rpdXmlResponse.getValue(), rpdXmlResponse.getId());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return items;
        }
        public FormValidation doCheckRoute(@QueryParameter String value) throws IOException, ServletException {

            if (value.length() == 0)
                return FormValidation.error("Specify RPD route.");
            return FormValidation.ok();

        }

        public FormValidation doCheckEnvironment(@QueryParameter String value) throws IOException, ServletException {

            if (value.length() == 0)
                return FormValidation.error("Specify RPD environment.");
            return FormValidation.ok();

        }
        public ListBoxModel doFillEnvironmentItems(@QueryParameter String environment, @QueryParameter String route, @QueryParameter String customProfile) {
            RPDPluginConfiguration currentProfile = config.getServerProfile(customProfile);
            ListBoxModel items;
            items = new ListBoxModel();
            items.add("", "");
            if(route == null)
                return items;
            try {
                RpdServerImpl serverInstance = new RpdServerImpl(currentProfile.getServerProfileName(), currentProfile.getServerURL(), currentProfile.getUserToken() );

                List<RPDXmlResponse> envs = serverInstance.executeRouteEnvironmentList(route);
                for (RPDXmlResponse rpdXmlResponse : envs) {
                    items.add(rpdXmlResponse.getValue(), rpdXmlResponse.getId());
                }

            } catch (Exception e) {
                System.out.println("No environments yet");
            }

            return items;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "RPD Deploy Instance";
        }

        @Override
        public RPDDeployInstance newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            Boolean showOptional = false;
            String customProfile = "";
            System.out.println(formData);
            if (formData.has("optionalProfile")) {
                customProfile = formData.getJSONObject("optionalProfile").getString("customProfile");
                showOptional = customProfile.isEmpty() ? false : true;
            }
            System.out.println(customProfile);
            return new RPDDeployInstance(formData.getString("instanceName"), formData.getString("pack"), formData.getString("environment"), formData.getString("route"), formData.getString("customProfile"), showOptional);
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
        System.out.println(serverProfile.getServerURL());
        System.out.println(serverProfile.getServerProfileName());
        envMap = build.getEnvironment(listener);

        try {
            if (getEnvironment() == null || getEnvironment().trim().length() == 0) {

                throw new Exception("The RPD Environment for the route must be selected in order to deploy.");

            }
            if (serverProfile == null) {
                listener.error("RPD Server URL or User Token is not specified or incorrect. Please check your configuration");
                return false;
            }
            listener.getLogger().println("Starting deployment of the RPD package " + getPack());
            String packageString = getPack().replaceAll(" ","_").replaceAll("-","_");
            String instanceID = serverProfile.resolveVariables("${RPD_"+packageString+"_instance_id}", envMap);
            System.out.println(packageString);
            System.out.println(instanceID);
            if(!instanceID.equalsIgnoreCase('"'+"${RPD_"+packageString+"_instance_id}"+'"'))
                deployPackageInstance(serverProfile, instanceID, listener);
            else
                deployPackageInstance(serverProfile, serverProfile.resolveVariables(getPack()+":"+getInstanceName(), envMap), listener);

        } catch (Exception e) {
            listener.error("Failed to create RPD instance " + e.getMessage());
            return false;
        }
        return true;
    }
    private void deployPackageInstance(RpdServerImpl serverProfile, String packageInstanceId, BuildListener listener)

            throws Exception {

        RPDXmlResponse rpdXmlResponse = serverProfile.executeInstanceDeploy(packageInstanceId, getRoute(), getEnvironment());

        listener.getLogger().println("Starting deployment of instance " + packageInstanceId + ". " + rpdXmlResponse.getValue());

        checkInstanceDeployStatus(serverProfile, rpdXmlResponse.getId(), listener);

    }
    private void checkInstanceDeployStatus(RpdServerImpl serverProfile, String deployInstanceId, BuildListener listener) throws Exception {

        String instanceStatus = "";

        do {

            Thread.sleep(5000);

            instanceStatus = serverProfile.executeInstanceDeployStatus(deployInstanceId);

        } while (!instanceStatus.equalsIgnoreCase("pass") && !instanceStatus.equalsIgnoreCase("fail") && !instanceStatus.equalsIgnoreCase("cancelled"));
        List<RPDXmlResponse> result = serverProfile.executeDeploymentLog(deployInstanceId);
        for (RPDXmlResponse message : result) {
            listener.getLogger().println("INFO: "+message.getValue());
        }

        if (instanceStatus.equalsIgnoreCase("fail") || instanceStatus.equalsIgnoreCase("cancelled")) {

            throw new Exception("Failure: RPD Instance Deployment has a status of " + instanceStatus);
        }
        listener.getLogger().println("RPD Instance Deployment has a status of " + instanceStatus);
    }

}

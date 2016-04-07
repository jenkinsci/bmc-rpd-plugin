package com.bmc.rpd.jenkins.plugin.bmcrpd.configuration;

import com.bmc.rpd.jenkins.plugin.bmcrpd.RpdServerImpl;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONString;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.XSD;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Locale;

/**
 * Created by aivani on 2/10/2016.
 */
@Extension
public class RPDPluginConfiguration extends GlobalConfiguration implements Serializable {
    private String serverURL;
    private String userToken;
    private String serverProfileName;
    private Boolean defaultProfile;

    public final CopyOnWriteList<RPDPluginConfiguration> serverProfiles = new CopyOnWriteList<RPDPluginConfiguration>();
    public static final String GLOBAL_PREFIX = "rpd.";

    public RPDPluginConfiguration (){
        load();
    }
    public RPDPluginConfiguration (String serverProfileName, String serverURL, String userToken, boolean defaultProfile ) {
        this.serverProfileName = serverProfileName;

        this.serverURL = serverURL;

        this.userToken = userToken;
        this.defaultProfile = defaultProfile;

    }
    public static RPDPluginConfiguration get() {
        return GlobalConfiguration.all().get(RPDPluginConfiguration.class);
    }

    public FormValidation doTestConnection(@QueryParameter("rpd.serverProfileName") final String serverProfileName, @QueryParameter("rpd.serverURL") final String serverURL,
                                           @QueryParameter("rpd.userToken") final String userToken)
            throws IOException, ServletException {

        try {
            RpdServerImpl serverProfile = new RpdServerImpl(serverProfileName, serverURL, userToken);
            serverProfile.testConnection();
            return FormValidation.okWithMarkup("<strong><font color='green'>Success</font></strong>");
        } catch (Exception e) {
            return FormValidation.error(e.getMessage());
        }

    }
    public String getServerProfileName() {
        return serverProfileName;
    }

    public String getServerURL() {
        return serverURL;
    }
    public Boolean isDefaultProfile(){
        return defaultProfile;
    }

    public String getUserToken() {
        return userToken;
    }
    public void setServerURL(String serverURL){
        this.serverURL = serverURL;
    }
    public void setUserToken(String userToken){
        this.userToken = userToken;
    }

    /**
     * The getter of the serverProfiles field.
     *
     * @return the value of the serverProfiles field.
     */
    public RPDPluginConfiguration[] getServerProfiles() {
        Iterator<RPDPluginConfiguration> it = serverProfiles.iterator();
        int size = 0;
        while (it.hasNext()) {
            it.next();
            size++;
        }
        return serverProfiles.toArray(new RPDPluginConfiguration[size]);
    }

    /**
     * This method returns object that matches the serverProfileName specified.
     * @param serverProfileName name of selected profile
     * @return the matching serverProfile or null
     */
    public RPDPluginConfiguration getServerProfile(String serverProfileName) {
        RPDPluginConfiguration[] servers = getServerProfiles();
        if (serverProfileName != null && serverProfileName.trim().length() > 0) {
            for (RPDPluginConfiguration server : servers) {
                if (server.getServerProfileName().equals(serverProfileName)) {
                    return server;
                }
            }
        }
        return null;
    }
    public RPDPluginConfiguration getDefaultServerProfile() {
        RPDPluginConfiguration[] servers = getServerProfiles();
            for (RPDPluginConfiguration server : servers) {
                if (server.isDefaultProfile()) {
                    return server;
                }
            }
        return null;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        serverProfiles.clear();
        if(!json.isEmpty()) {
                Object j = json.get("serverProfile");
                if(j instanceof net.sf.json.JSONArray) {
                    JSONArray request = json.getJSONArray("serverProfile");
                    boolean defaultSet = false;
                    for (int i = 0; i < request.size(); i++) {
                        if(request.getJSONObject(i).getBoolean("defaultProfile"))
                            defaultSet = true;
                    }
                    for (int i = 0; i < request.size(); i++) {
                        if(i==0 && !defaultSet)
                            serverProfiles.add(new RPDPluginConfiguration(request.getJSONObject(i).getString("serverProfileName"), request.getJSONObject(i).getString("serverURL"), request.getJSONObject(i).getString("userToken"), true ));
                        else
                            serverProfiles.add(new RPDPluginConfiguration(request.getJSONObject(i).getString("serverProfileName"), request.getJSONObject(i).getString("serverURL"), request.getJSONObject(i).getString("userToken"), request.getJSONObject(i).getBoolean("defaultProfile")));
                    }
                }
                else{
                    json = json.getJSONObject("serverProfile");
                    serverProfiles.add(new RPDPluginConfiguration(json.getString("serverProfileName"), json.getString("serverURL"), json.getString("userToken"), true));
                }
        }

        save();
        return true;
    }
}

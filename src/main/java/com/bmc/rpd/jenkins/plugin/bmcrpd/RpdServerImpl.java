package com.bmc.rpd.jenkins.plugin.bmcrpd;

import com.bmc.rpd.jenkins.plugin.bmcrpd.xml.RPDCommand;
import com.bmc.rpd.jenkins.plugin.bmcrpd.xml.RPDXmlResponse;
import com.bmc.rpd.jenkins.plugin.bmcrpd.xml.RPDXmlResult;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aivani on 2/4/2016.
 */
public class RpdServerImpl implements Serializable {
    /** The url. */
    private static final long serialVersionUID = -8723534991244260459L;
    /** The server profile name. */
    private String serverProfileName;

    private String serverURL;
    private Map<String, String> envMap = null;


    /** The apiToken used to make REST calls. */

    private String userToken;
    public RpdServerImpl() {



    }
    /**

     * Instantiates a new BMC RPD Server Location.

     *

     * @param serverURL

     *          the serverURL of the RPD install

     * @param userToken

     *          the userToken

     */


    public RpdServerImpl(String serverProfileName, String serverURL, String userToken ) {
        this.serverProfileName = serverProfileName;

        this.serverURL = serverURL;

        this.userToken = userToken;

    }

    public String getServerProfileName() {

        return serverProfileName;

    }
    public void setServerProfileName(String serverProfileName) {

        this.serverProfileName = serverProfileName;

    }
    /**

     * Gets the serverURL.

     *

     * @return the serverURL

     */

    public String getServerURL() {

        return serverURL;

    }
    public String getDisplayName() {

        if (StringUtils.isEmpty(serverProfileName)) {

            return serverURL;

        } else {

            return serverProfileName;

        }

    }



    /**

     * Sets the url.

     *

     * @param serverURL

     *          the new serverURL

     */

    public void setServerURL(String serverURL) {

        this.serverURL = serverURL;

        if (this.serverURL != null) {

            this.serverURL = this.serverURL.replaceAll("\\\\", "/");

        }

        while (this.serverURL != null && this.serverURL.endsWith("/")) {

            this.serverURL = this.serverURL.substring(0, this.serverURL.length() - 1);

        }

    }



    /**

     * Gets the userToken.

     *

     * @return the userToken

     */

    public String getUserToken() {

        return userToken;

    }



    /**

     * Sets the userToken.

     *

     * @param userToken

     *          the new userToken

     */

    public void setUserToken(String userToken) {

        this.userToken = userToken;

    }

    public List<RPDXmlResponse> testConnection() throws Exception {
        RPDCommand rpdCmd = RPDCommand.getRpdCommandUsingApiToken(serverURL, userToken, RPDCommand.PACKAGE_LIST);

        RPDXmlResult result = rpdCmd.executeResponseToMap();

        return result.getResponses();
    }

    public List<RPDXmlResponse> packageListWithPartial(String partial) throws Exception {
        String cmdWithPartial = RPDCommand.PACKAGE_LIST + " %"+partial+"%";

        RPDCommand rpdCmd = RPDCommand.getRpdCommandUsingApiToken(serverURL, userToken, cmdWithPartial);

        RPDXmlResult result = rpdCmd.executeResponseToMap();
        return result.getResponses();
    }

    public RPDXmlResponse executePackageInstanceCreate(String rpdPackage, String InstanceName) throws Exception {

        try {
            RPDCommand rpdCmd;
            System.out.println("Running instance create");
            if(InstanceName.isEmpty()) {
                rpdCmd = RPDCommand.getRpdCommandUsingApiToken(serverURL, userToken, RPDCommand.PACKAGE_INSTANCE_CREATE, '"'+rpdPackage+'"');
            }
            else {
                rpdCmd = RPDCommand.getRpdCommandUsingApiToken(serverURL, userToken, RPDCommand.PACKAGE_INSTANCE_CREATE, '"'+rpdPackage+'"', InstanceName);
            }

            RPDXmlResult result = rpdCmd.executeResponseToMap();
            return result.getFirstResponse();
        }catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    public List<RPDXmlResponse> executeInstanceLog(String rpdInstance) throws Exception {

        try {
            RPDCommand rpdCmd = RPDCommand.getRpdCommandUsingApiToken(serverURL, userToken, RPDCommand.INSTANCE_LOG, rpdInstance, "1");
            RPDXmlResult result = rpdCmd.executeResponseToMap();
            return result.getResponses();
        }catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    public List<RPDXmlResponse> executeDeploymentLog(String deploymentId) throws Exception {

        try {
            RPDCommand rpdCmd = RPDCommand.getRpdCommandUsingApiToken(serverURL, userToken, RPDCommand.DEPLOYMENT_LOG, deploymentId, "1");
            RPDXmlResult result = rpdCmd.executeResponseToMap();
            return result.getResponses();
        }catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    public String executeInstanceCreateStatus(String packageInstanceId) throws Exception {

        return executeInstanceStatus(packageInstanceId, RPDCommand.INSTANCE_CREATE_STATUS);
    }

    public String executeInstanceStatus(String id, String command) throws Exception {

        RPDCommand rpdCmd = RPDCommand.getRpdCommandUsingApiToken(serverURL, userToken, command, id);

        RPDXmlResult result = rpdCmd.executeResponseToMap();

        RPDXmlResponse response = result.getFirstResponse();

        return response.getValue().split(":")[0];

    }
    public List<RPDXmlResponse> executeRouteList() throws Exception {

        RPDCommand rpdCmd = RPDCommand.getRpdCommandUsingApiToken(serverURL, userToken, RPDCommand.ROUTE_LIST);

        RPDXmlResult result = rpdCmd.executeResponseToMap();

        return result.getResponses();
    }

    public List<RPDXmlResponse> executeRouteEnvironmentList(String rpdRoute) throws Exception {

        RPDCommand rpdCmd = RPDCommand.getRpdCommandUsingApiToken(serverURL, userToken, RPDCommand.ROUTE_ENV_LIST, rpdRoute);

        RPDXmlResult result = rpdCmd.executeResponseToMap();

        return result.getResponses();

    }

    public RPDXmlResponse executeInstanceDeploy(String packageInstanceId, String route, String environment) throws Exception {

        RPDCommand rpdCmd = RPDCommand.getRpdCommandUsingApiToken(serverURL, userToken, RPDCommand.INSTANCE_DEPLOY, packageInstanceId, route, environment);

        RPDXmlResult result = rpdCmd.executeResponseToMap();

        return result.getFirstResponse();
    }

    public String executeInstanceDeployStatus(String deploymentInstanceId) throws Exception {

        return executeInstanceStatus(deploymentInstanceId, RPDCommand.INSTANCE_DEPLOY_STATUS);
    }
    public String resolveVariables(String pureText, Map<String, String> envMap) {
        if(pureText.isEmpty())
            return '"'+pureText+'"';
        String REGEX = "\\$\\{\\w+\\}";
        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(pureText);
        StringBuffer result = new StringBuffer();
        while (m.find()) {
            if(envMap.containsKey(m.group().replaceAll("\\$\\{|\\}","")))
                m.appendReplacement(result,envMap.get(m.group().replaceAll("\\$\\{|\\}","")));

        }
        m.appendTail(result);

        return '"'+result.toString()+'"';
    }


    }


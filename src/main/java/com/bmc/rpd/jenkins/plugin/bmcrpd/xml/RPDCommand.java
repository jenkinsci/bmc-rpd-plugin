package com.bmc.rpd.jenkins.plugin.bmcrpd.xml;


import com.thoughtworks.xstream.XStream;
import hudson.model.Items;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aivani on 2/4/2016.
 */
public class RPDCommand {
    public static final String PACKAGE_LIST = "package list";

    public static final String ROUTE_LIST = "route list";

    public static final String ROUTE_ENV_LIST = "route next environment list";

    public static final String PACKAGE_INSTANCE_CREATE = "instance create package";

    public static final String INSTANCE_CREATE_STATUS = "instance status";

    public static final String INSTANCE_DEPLOY_STATUS = "deploy status";

    public static final String INSTANCE_DEPLOY = "instance deploy";

    public static final String INSTANCE_LOG = "instance log";

    public static final String DEPLOYMENT_LOG = "process deployment log";

    private static final String requestXmlDocTemplate = "<q auth=\"%s\"><request command=\"%s\">%s</request></q>";

    private static final String requestArgsTemplate = "<arg>%s</arg>";

    private URI uri = null;

    private String requestXmlDoc = null;


    private RPDCommand(URI uri, String requestXmlDoc) {

        this.uri = uri;

        this.requestXmlDoc = requestXmlDoc;

    }

    public static RPDCommand getRpdCommandUsingApiToken(String url, String apiToken, String command, String... args) throws Exception {



        if (url == null || url.trim().length() == 0) {

            throw new Exception("RPD Server URL field is required to invoke RPD!");

        }



        if (apiToken == null || apiToken.trim().length() == 0) {

            throw new Exception("RPD User Token field is required to invoke RPD!");

        }



        if (command == null || command.trim().length() == 0) {

            throw new Exception("command field is required to invoke RPD!");

        }



        String requestArgs = "";

        if (args != null) {

            for (String arg : args) {

                requestArgs += String.format(requestArgsTemplate, arg);

            }

        }



        URI uri = new URI(url);

        return new RPDCommand (uri, String.format(requestXmlDocTemplate, apiToken, command, requestArgs));



    }

    public RPDXmlResult executeResponseToMap() throws Exception {
        try{
            return parseXmlResponseToMap(executeRpdPost(this.uri, this.requestXmlDoc));
        }
        catch (Exception e) {
            throw e;
        }
    }
    private RPDXmlResult parseXmlResponseToMap(String xmlResponse) throws Exception {

        RPDXmlResult result;

        try {

            //XStream xStream = new XStream(new DomDriver());

            XStream xStream = Items.XSTREAM;

            xStream.registerConverter(new RPDXmlConverter());

            xStream.alias("q", RPDXmlResult.class);

            result = (RPDXmlResult) xStream.fromXML(xmlResponse);

            if (result == null) {

                throw new Exception("No RPD Results Found.");

            } else if (!result.getResultCode().equalsIgnoreCase("0")) {

                throw new Exception(result.getResultMessage()+": "+result.getFirstResponse().getValue());

            }

        } catch (Exception e) {

            // TODO Auto-generated catch block

            //e.printStackTrace();

            throw e;

        }





        return result;

    }
    private String executeRpdPost(URI uri, String postContents) throws Exception {

        String result = null;

        HttpClient httpClient = new HttpClient();




        PostMethod method = new PostMethod(uri.toString());

        method.setRequestBody(postContents);

        method.setRequestHeader("Content-Type", "text/xml");

        method.setRequestHeader("charset", "utf-8");



        try {

//            HttpClientParams params = httpClient.getParams();

//            params.setAuthenticationPreemptive(true);

//            UsernamePasswordCredentials clientCredentials = new UsernamePasswordCredentials(userName, password);

//            httpClient.getState().setCredentials(AuthScope.ANY, clientCredentials);



            int responseCode = httpClient.executeMethod(method);


            if (responseCode == 401) {

                throw new Exception("Error connecting to RPD: Invalid API Token");

            }

            else if (responseCode != 200) {

                throw new Exception("Error connecting to RPD: " + responseCode);

            }

            else {

                result = method.getResponseBodyAsString();

            }

        }

        finally {

            method.releaseConnection();

        }



        return result;



    }


}

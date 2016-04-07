package com.bmc.rpd.jenkins.plugin.bmcrpd.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aivani on 2/4/2016.
 */
public class RPDXmlResult {
    String auth;

    String rpdVersion;



    String requestCommand;

    String resultCode;

    String resultMessage;



    List<String> arguments = new ArrayList<String>();



    List<RPDXmlResponse> responses = new ArrayList<RPDXmlResponse>();



    public String getAuth() {

        return auth;

    }



    public void setAuth(String auth) {

        this.auth = auth;

    }



    public String getRpdVersion() {

        return rpdVersion;

    }



    public void setRpdVersion(String rpdVersion) {

        this.rpdVersion = rpdVersion;

    }



    public String getRequestCommand() {

        return requestCommand;

    }



    public void setRequestCommand(String requestCommand) {

        this.requestCommand = requestCommand;

    }



    public String getResultCode() {

        if (resultCode == null)

            return "-1";



        return resultCode;

    }



    public void setResultCode(String resultCode) {

        this.resultCode = resultCode;

    }



    public String getResultMessage() {

        if (resultMessage == null)

            return "No Detailed Message Set.";



        return resultMessage;

    }



    public void setResultMessage(String resultMessage) {

        this.resultMessage = resultMessage;

    }



    public List<RPDXmlResponse> getResponses() {

        return responses;

    }



    public void addResponse(String id, String value) {

        responses.add(new RPDXmlResponse(id, value));

    }



    public List<String> getArguments() {

        return arguments;

    }



    public void addArgument(String argument) {

        this.arguments.add(argument);

    }



    public RPDXmlResponse getFirstResponse() throws Exception {
        if (this.getResponses().size() >= 1) {

            return this.getResponses().get(0);

        } else {

            throw new Exception("No Reponse came back from Package Instance Create.");

        }



    }
}

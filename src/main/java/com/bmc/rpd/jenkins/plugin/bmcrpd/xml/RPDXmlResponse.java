package com.bmc.rpd.jenkins.plugin.bmcrpd.xml;

/**
 * Created by aivani on 2/4/2016.
 */
public class RPDXmlResponse {
    String id;

    String value;



    public RPDXmlResponse(String id, String value) {

        this.id = id;

        this.value = value;

    }



    public String getId() {

        return id;

    }

    public void setId(String id) {

        this.id = id;

    }

    public String getValue() {

        return value;

    }

    public void setValue(String value) {

        this.value = value;

    }
}

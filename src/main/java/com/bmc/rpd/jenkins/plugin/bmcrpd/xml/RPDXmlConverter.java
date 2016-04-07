package com.bmc.rpd.jenkins.plugin.bmcrpd.xml;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.awt.*;

/**
 * Created by aivani on 2/4/2016.
 */
public class RPDXmlConverter implements Converter {
    public boolean canConvert(Class clazz) {

            return RPDXmlResult.class == clazz;

    }
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {

    }



    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

        RPDXmlResult result = new RPDXmlResult();



        result.setAuth(reader.getAttribute("auth"));

        result.setRpdVersion(reader.getAttribute("Version"));



        while (reader.hasMoreChildren()) {

            reader.moveDown();

            if ("request".equalsIgnoreCase(reader.getNodeName())) {

                result.setRequestCommand(reader.getAttribute("command"));



                processRequestArguments(result, reader);

            } else if ("result".equalsIgnoreCase(reader.getNodeName())) {

                result.setResultCode(reader.getAttribute("rc"));

                result.setResultMessage(reader.getAttribute("message"));

                processResultResponses(result, reader);



            }

            reader.moveUp();

        }



        return result;

    }



    private void processRequestArguments(RPDXmlResult result, HierarchicalStreamReader reader) {

        while (reader.hasMoreChildren()) {

            reader.moveDown();

            if ("arg".equalsIgnoreCase(reader.getNodeName())) {

                result.addArgument(reader.getValue());

            }



            reader.moveUp();

        }





    }



    private void processResultResponses(RPDXmlResult result, HierarchicalStreamReader reader) {

        while (reader.hasMoreChildren()) {

            reader.moveDown();

            if ("response".equalsIgnoreCase(reader.getNodeName())) {

                if (reader.getAttribute("value") != null){

                    result.addResponse(reader.getAttribute("id"), reader.getAttribute("value"));
                }
                else if(reader.getValue().toString().isEmpty()){
                    result.addResponse(null,null);
                }
                else if("3".equalsIgnoreCase(result.getResultCode())){

                    result.addResponse("Operation failed", reader.getValue());

                }
                else if ("instance log".equalsIgnoreCase(result.getRequestCommand())||"process deployment log".equalsIgnoreCase(result.getRequestCommand())) {

                    result.addResponse("instance log", reader.getValue().substring(2));
                }
                else {
                    result.addResponse("error", reader.getValue());
                }
            }



            reader.moveUp();

        }





    }

}

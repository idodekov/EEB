package org.redoubt.application.configuration;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlConfigurationManager extends BaseConfigurationManager {
    private static final Logger sLogger = Logger.getLogger(XmlConfigurationManager.class);
    
    private static final String XML_ELEMENT_CONFIGURATION_OPTION = "ConfigurationOption";
    private static final String XML_ATTRIBUTE_NAME = "name";
    private static final String XML_ATTRIBUTE_VALUE = "value";
    
    @Override
    public void loadConfiguration() {
        try {
            File fXmlFile = new File(ConfigurationConstants.CONFIGURATION_FILE_GLOBAL_CONFIGURATION);
            sLogger.info("Loading configuration parameters from file [" + ConfigurationConstants.CONFIGURATION_FILE_GLOBAL_CONFIGURATION + "].");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            
            doc.getDocumentElement().normalize();
            
            sLogger.debug("Root element :" + doc.getDocumentElement().getNodeName());
            
            NodeList nList = doc.getElementsByTagName(XML_ELEMENT_CONFIGURATION_OPTION);
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                
                sLogger.debug("Current Element :" + nNode.getNodeName());
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    
                    String name = eElement.getAttribute(XML_ATTRIBUTE_NAME);
                    String value = eElement.getAttribute(XML_ATTRIBUTE_VALUE);
                    
                    sLogger.debug("Adding the following server configuration option: " + name + "=" + value + ".");
                    setConfigurationOption(name, value);
                }
            }
        } catch (Exception e) {
            sLogger.error("Error reading file [" + ConfigurationConstants.CONFIGURATION_FILE_GLOBAL_CONFIGURATION + "]. " + e.getMessage(), e);
        }
        
    }

}

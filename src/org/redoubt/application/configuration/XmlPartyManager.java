package org.redoubt.application.configuration;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.redoubt.api.factory.Factory;
import org.redoubt.api.factory.FactoryConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlPartyManager extends BasePartyManager {
	private static final Logger sLogger = Logger.getLogger(XmlPartyManager.class);
	
	private static final String XML_ELEMENT_PARTY = "Party";
	private static final String XML_ELEMENT_SETTING = "Setting";
	private static final String XML_ATTRIBUTE_NAME = "name";
    
	@Override
	public void loadParties() {
		try {
			File fXmlFile = new File(ConfigurationConstants.CONFIGURATION_FILE_PARTIES);
            sLogger.info("Loading configuration parameters from file [" + ConfigurationConstants.CONFIGURATION_FILE_PARTIES + "].");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            
            doc.getDocumentElement().normalize();
            
            sLogger.debug("Root element :" + doc.getDocumentElement().getNodeName());
            
            NodeList nList = doc.getElementsByTagName(XML_ELEMENT_PARTY);
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				
				sLogger.debug("Current Element :" + nNode.getNodeName());
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					
					Party party = Factory.getInstance().getParty(FactoryConstants.PARTY_TYPE_BASE);
					
					NodeList settingsList =  eElement.getChildNodes();
					for (int i = 0; i < settingsList.getLength(); i++) {
						Node currentNode = settingsList.item(i);
						if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
							Element currentElement = (Element) currentNode;
							
							if(XML_ELEMENT_SETTING.equals(currentElement.getTagName())) {
								String key = currentElement.getAttribute(XML_ATTRIBUTE_NAME);
								String value = currentElement.getTextContent();
								
								sLogger.debug("Adding party setting: " + key + "=" + value + ".");
								party.put(key, value);
							}
						}
					}
					
					if(party.getPartyId() == null || party.getPartyId().trim().isEmpty()) {
						sLogger.error("A party doesn't have a partyId value set. This is not allowed. The party definition is discarded.");
						continue;
					}
					
					addParty(party);
					sLogger.info("Party with ID [" + party.getPartyId() + "] was created.");
				}
				
			}
		} catch (Exception e) {
            sLogger.error("Error reading file [" + ConfigurationConstants.CONFIGURATION_FILE_PARTIES + "]. " + e.getMessage(), e);
        }

	}

}

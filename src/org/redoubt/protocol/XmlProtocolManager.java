package org.redoubt.protocol;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.redoubt.api.factory.Factory;
import org.redoubt.api.transport.ITransport;
import org.redoubt.transport.SettingsHolder;
import org.redoubt.transport.TransportException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlProtocolManager extends BaseProtocolManager {
	private static final Logger sLogger = Logger.getLogger(XmlProtocolManager.class);
	
	private static final String XML_ELEMENT_TRANSPORT = "Transport";
	private static final String XML_ELEMENT_SETTING = "Setting";
	private static final String XML_ELEMENT_PROTOCOL = "Protocol";
	private static final String XML_ATTRIBUTE_NAME = "name";
	private static final String XML_ATTRIBUTE_TYPE = "type";
	
	public XmlProtocolManager() {
		super();
	}
	
	@Override
    public void loadTransports() {
	    List<SettingsHolder> settings = loadTransportsFromFile();
        Iterator<SettingsHolder> it = settings.iterator();
        while(it.hasNext()) {
            SettingsHolder currentSettings = it.next();
            
            try {
                ITransport transport = Factory.getInstance().buildTransport(currentSettings);
                addTransport(transport);
            } catch (ProtocolException | TransportException e) {
                sLogger.error("An error has occurred while loading transport. " + e.getMessage(), e);
            }
        }
    }
	
	private List<SettingsHolder> loadTransportsFromFile() {
		List<SettingsHolder> resultList = new ArrayList<SettingsHolder>();
		try {
			File fXmlFile = new File("conf/transports.xml");
			sLogger.info("Loading transports from file transports.xml.");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			
			doc.getDocumentElement().normalize();
			
			sLogger.debug("Root element :" + doc.getDocumentElement().getNodeName());
			
			NodeList nList = doc.getElementsByTagName(XML_ELEMENT_TRANSPORT);
			for (int temp = 0; temp < nList.getLength(); temp++) {
				SettingsHolder transportSettings = null;
				SettingsHolder protocolSettings = null;
				
				Node nNode = nList.item(temp);
				
				sLogger.debug("Current Element :" + nNode.getNodeName());
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					
					String transportType = eElement.getAttribute(XML_ATTRIBUTE_TYPE);
					sLogger.debug("Transport type is: " + transportType + ".");
					transportSettings = Factory.getInstance().getTransportSettings(transportType);
					if(transportSettings == null) {
						throw new ProtocolException("Unknown transport type " + transportType + ".");
					}
					
					transportSettings.put(SettingsHolder.SettingsKeyring.TYPE, transportType);
					
					NodeList settingsList =  eElement.getChildNodes();
					for (int i = 0; i < settingsList.getLength(); i++) {
						Node currentNode = settingsList.item(i);
						if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
							Element currentElement = (Element) currentNode;
							
							if(XML_ELEMENT_SETTING.equals(currentElement.getTagName())) {
								String key = currentElement.getAttribute(XML_ATTRIBUTE_NAME);
								String value = currentElement.getTextContent();
								
								sLogger.debug("Adding transport setting: " + key + "=" + value + ".");
								transportSettings.put(key, value);
							} else if(XML_ELEMENT_PROTOCOL.equals(currentElement.getTagName())) {
								String protocolType = currentElement.getAttribute(XML_ATTRIBUTE_TYPE);
								sLogger.debug("Protocol type is: " + protocolType + ".");
								protocolSettings = Factory.getInstance().getProtocolSettings(protocolType);
								if(protocolSettings == null) {
									throw new ProtocolException("Unknown protocol type " + protocolType + ".");
								}
								protocolSettings.put(SettingsHolder.SettingsKeyring.TYPE, protocolType);
								
								NodeList protocolSettingsList =  currentElement.getElementsByTagName(XML_ELEMENT_SETTING);
								for (int j = 0; j < protocolSettingsList.getLength(); j++) {
									Node settingNode = protocolSettingsList.item(j);
									if (nNode.getNodeType() == Node.ELEMENT_NODE) {
										Element settingElement = (Element) settingNode;
										
										String key = settingElement.getAttribute(XML_ATTRIBUTE_NAME);
										String value = settingElement.getTextContent();
										
										sLogger.debug("Adding protocol setting: " + key + "=" + value + ".");
										protocolSettings.put(key, value);
									}
								}
							}
							
							
						}
					}
					
				}
				
				
				transportSettings.put(SettingsHolder.SettingsKeyring.PRTOCOL_SETTINGS, protocolSettings);
				resultList.add(transportSettings);
			}
			
			sLogger.info("Successfully loaded transports from file transports.xml.");
		} catch(Exception e) {
			sLogger.error("Error reading file transports.xml. " + e.getMessage(), e);
		}
		
		return resultList;
	}

}

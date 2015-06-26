package org.redoubt.api.factory;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.redoubt.api.configuration.IServerConfigurationManager;
import org.redoubt.api.protocol.IProtocol;
import org.redoubt.api.protocol.IProtocolManager;
import org.redoubt.api.protocol.IProtocolSettings;
import org.redoubt.api.transport.ITransport;
import org.redoubt.api.transport.ITransportSettings;
import org.redoubt.application.configuration.XmlConfigurationManager;
import org.redoubt.protocol.ProtocolException;
import org.redoubt.protocol.XmlProtocolManager;
import org.redoubt.protocol.as2.As2HttpListener;
import org.redoubt.protocol.as2.As2Protocol;
import org.redoubt.protocol.as2.As2ProtocolSettings;
import org.redoubt.transport.SettingsHolder;
import org.redoubt.transport.TransportException;
import org.redoubt.transport.http.HttpTransport;
import org.redoubt.transport.http.HttpTransportSettings;

public class Factory {
	private static Factory sInstance;
	private static IProtocolManager sProtocolManager;
	private static IServerConfigurationManager sServerConfigurationManager;
	private static final Object SINGLETON_LOCK = new Object();
	
	private static final Logger sLogger = Logger.getLogger(Factory.class);
	
	private Factory(){}

	public static Factory getInstance() {
		if(sInstance == null) {
			synchronized(SINGLETON_LOCK) {
				if(sInstance == null) {
					sLogger.info("Initializing Factory instance...");
					sInstance = new Factory();			
					sLogger.info("Factory instance successfully initialized.");
				}
			}
		}
		
		return sInstance;
	}
	
	public IProtocolManager getProtocolManager(String type) {
		if(sProtocolManager == null) {
			synchronized(SINGLETON_LOCK) {
				if(sProtocolManager == null) {
					sLogger.info("Initializing ProtocolManager instance...");
					
					if(FactoryConstants.PROTOCOL_MANAGER_XML.equals(type)) {
						sProtocolManager = new XmlProtocolManager();
					}
					
					sProtocolManager.loadTransports();
					
					sLogger.info("ProtocolManager instance successfully initialized.");
				}
			}
		}
		
		return sProtocolManager;
	}
	
	public IServerConfigurationManager getServerConfigurationManager(String type) {
        if(sServerConfigurationManager == null) {
            synchronized(SINGLETON_LOCK) {
                if(sServerConfigurationManager == null) {
                    sLogger.info("Initializing ServerConfigurationManager instance...");
                    
                    if(FactoryConstants.SERVER_CONFIGURATION_MANAGER_XML.equals(type)) {
                        sServerConfigurationManager = new XmlConfigurationManager();
                    }
                    
                    sServerConfigurationManager.loadConfiguration();
                    
                    sLogger.info("ServerConfigurationManager instance successfully initialized.");
                }
            }
        }
        
        return sServerConfigurationManager;
    }
	
	public ITransport buildTransport(SettingsHolder baseTransportSettings) 
			throws ProtocolException, TransportException {
		ITransportSettings transportSettings = (ITransportSettings) baseTransportSettings;
		SettingsHolder baseProtocolSettings = (SettingsHolder) baseTransportSettings.get(SettingsHolder.SettingsKeyring.PRTOCOL_SETTINGS);
		IProtocolSettings protocolSettings = (IProtocolSettings) baseProtocolSettings;
		
		
		IProtocol protocol = getProtocol(protocolSettings.getProtocolName());
		ITransport transport = getTransport(transportSettings.getTransportName());
		transport.setProtocol(protocol);
		
		protocol.init(protocolSettings);
		transport.init(transportSettings);
		
		return transport;
	}
	
	public ITransport getTransport(String transportName) {
		if(HttpTransportSettings.TRANSPORT_NAME.equals(transportName)) {
			return new HttpTransport();
		}
		
		return null;
	}
	
	public IProtocol getProtocol(String protocolName) {
		if(As2ProtocolSettings.PROTOCOL_NAME.equals(protocolName)) {
			return new As2Protocol();
		}
		
		return null;
	}
	
	
	public SettingsHolder getTransportSettings(String transportType) {
		if(HttpTransportSettings.TRANSPORT_NAME.equals(transportType)) {
			return new HttpTransportSettings();
		}
		
		return null;
	}
	
	public SettingsHolder getProtocolSettings(String protocolType) {
		if(As2ProtocolSettings.PROTOCOL_NAME.equals(protocolType)) {
			return new As2ProtocolSettings();
		}
		
		return null;
	}
	
	public HttpServlet getHttpListener(HttpTransportSettings settings) {
	    IProtocolSettings protocolSettings = (IProtocolSettings) settings.getProtocolSettings();
	    String protocolName = protocolSettings.getProtocolName();
	    
	    if(As2ProtocolSettings.PROTOCOL_NAME.equals(protocolName)) {
            return new As2HttpListener(settings);
        }
	    
	    return null;
	}
}

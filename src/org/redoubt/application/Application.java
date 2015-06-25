package org.redoubt.application;

import org.apache.log4j.Logger;
import org.redoubt.api.factory.Factory;
import org.redoubt.api.factory.FactoryConstants;
import org.redoubt.api.protocol.IProtocolManager;
import org.redoubt.application.logging.LoggingUtils;

public class Application {
	private static final Logger sLogger = Logger.getLogger(Application.class);

	public static void main(String[] args) {
		LoggingUtils.initializeLogging();
		
		sLogger.info("Starting " + VersionInformation.APP_NAME + " [" + VersionInformation.APP_VERSION + "]...");
		
		IProtocolManager protocolManager = Factory.getInstance().getProtocolManager(FactoryConstants.PROTOCOL_MANAGER_XML);
		
		protocolManager.loadTransports();
		protocolManager.startTransports();
		
		sLogger.info("Done starting server.");
		
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			sLogger.error(e.getMessage(), e);
		}
	}

}

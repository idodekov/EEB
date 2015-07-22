package org.redoubt.application;

import java.security.Security;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.redoubt.api.configuration.IServerConfigurationManager;
import org.redoubt.api.factory.Factory;
import org.redoubt.api.protocol.IProtocolManager;
import org.redoubt.application.logging.LoggingUtils;

public class Application {
	private static final Logger sLogger = Logger.getLogger(Application.class);

	public static void main(String[] args) {
		LoggingUtils.initializeLogging();
		
		sLogger.info("Starting " + VersionInformation.APP_NAME + " [" + VersionInformation.APP_VERSION + "]...");
		
		sLogger.info("Adding BC security provider...");
		Security.addProvider(new BouncyCastleProvider());
		sLogger.info("BC security provider succesfully added.");
		
		IServerConfigurationManager configurationManager = Factory.getInstance().getServerConfigurationManager();
		
		//TODO: Remove
		Factory.getInstance().getCertificateManager();
		
		IProtocolManager protocolManager = Factory.getInstance().getProtocolManager();
		
		protocolManager.startTransports();
		
		ShutdownHook shutdownHook = new ShutdownHook(configurationManager.getShutDownPort());
		shutdownHook.setName("ShutdownHook");
		shutdownHook.start();
		
		sLogger.info("Done starting server.");
		
		try {
		    shutdownHook.join();
		} catch (InterruptedException e) {
			sLogger.error(e.getMessage(), e);
		}
		
		protocolManager.stopTransports();
		sLogger.info("All transports have been stopped. Server shutdown complete.");
	}

}

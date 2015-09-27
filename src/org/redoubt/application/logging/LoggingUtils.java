package org.redoubt.application.logging;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.redoubt.application.configuration.ConfigurationConstants;

public class LoggingUtils {
	private static final Logger sLogger = Logger.getLogger(LoggingUtils.class);
	
	public static void initializeLogging() {
		DOMConfigurator.configureAndWatch(ConfigurationConstants.CONFIGURATION_FILE_LOG4J);
		sLogger.info("log4j system initialized.");
	}
	
	public static void suppressLogging() {
	    //TODO
    }
}

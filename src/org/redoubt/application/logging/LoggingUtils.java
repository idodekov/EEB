package org.redoubt.application.logging;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class LoggingUtils {
	private static final Logger sLogger = Logger.getLogger(LoggingUtils.class);
	
	public static void initializeLogging() {
		BasicConfigurator.configure();
		sLogger.info("log4j system initialized.");
	}
	
	public static void suppressLogging() {
	    //TODO
    }
}

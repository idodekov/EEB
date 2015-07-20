package org.redoubt.application.configuration;

public class ConfigurationConstants {
    private ConfigurationConstants() {}
    
    public static final String CONFIGURATION_FILE_TRANSPORTS = "conf/transports.xml";
    public static final String CONFIGURATION_FILE_GLOBAL_CONFIGURATION = "conf/global-configuration.xml";
    
    public static final String CONFIGURATION_OPTION_WORK_FOLDER = "WorkFolder";
    public static final String CONFIGURATION_OPTION_BACKUP_FOLDER = "BackupFolder";
    public static final String CONFIGURATION_OPTION_DO_BACKUP = "DoBackup";
    public static final String CONFIGURATION_OPTION_SHUTDOWN_PORT = "ShutdownPort";
    
    public static final String SHUTDOWN_COMMAND = "shutdown";
    
    public static final String DIRECTION_INBOUND = "inbound";
    public static final String DIRECTION_OUTBOUND = "outbound";
}

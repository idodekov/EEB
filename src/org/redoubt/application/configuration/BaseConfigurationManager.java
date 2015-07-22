package org.redoubt.application.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.redoubt.api.configuration.IServerConfigurationManager;

public abstract class BaseConfigurationManager implements IServerConfigurationManager {
    private static final Logger sLogger = Logger.getLogger(BaseConfigurationManager.class);
    private Map<String, String> serverConfiguration;
    
    public BaseConfigurationManager() {
        serverConfiguration = new HashMap<String, String>();
    }

    @Override
    public abstract void loadConfiguration();

    @Override
    public String getConfigurationOption(String name) {
        return serverConfiguration.get(name);
    }

    @Override
    public void setConfigurationOption(String name, String value) {
        serverConfiguration.put(name, value);
    }

    @Override
    public Path getWorkFolder() {
        String transportWorkFolder = getConfigurationOption(ConfigurationConstants.CONFIGURATION_OPTION_WORK_FOLDER);
        Path workFolder = Paths.get(transportWorkFolder);
        if(!Files.exists(workFolder)) {
            try {
                Files.createDirectory(workFolder);
            } catch (IOException e) {
                sLogger.error("Error while  creating WORK folder. " + e.getMessage(), e);
            }
        }
        return workFolder;
    }

    @Override
    public Path getBackupFolder() {
        String protocolBackupFolder = getConfigurationOption(ConfigurationConstants.CONFIGURATION_OPTION_BACKUP_FOLDER);
        
        Path backupFolder = Paths.get(protocolBackupFolder);
        if(!Files.exists(backupFolder)) {
            try {
                Files.createDirectory(backupFolder);
            } catch (IOException e) {
                sLogger.error("Error while creating BACKUP folder. " + e.getMessage(), e);
            }
        }
        
        return backupFolder;
    }
    
    @Override
    public boolean doBackup() {
        String doBackup = getConfigurationOption(ConfigurationConstants.CONFIGURATION_OPTION_DO_BACKUP);
        return "true".equalsIgnoreCase(doBackup);
    }

    @Override
    public int getShutDownPort() {
        return Integer.parseInt(getConfigurationOption(ConfigurationConstants.CONFIGURATION_OPTION_SHUTDOWN_PORT));
    }
    
    @Override
    public Path getKeystoreFile() {
        String keystoreFile = getConfigurationOption(ConfigurationConstants.CONFIGURATION_OPTION_KEYSTORE_FILE);
        return Paths.get(keystoreFile);
    }
    
    @Override
    public String getKeystorePassword() {
        return getConfigurationOption(ConfigurationConstants.CONFIGURATION_OPTION_KEYSTORE_PASSWORD);
    }

}

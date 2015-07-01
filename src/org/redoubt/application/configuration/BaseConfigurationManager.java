package org.redoubt.application.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.redoubt.api.configuration.IServerConfigurationManager;

public abstract class BaseConfigurationManager implements IServerConfigurationManager {
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
        return Paths.get(transportWorkFolder);
    }

    @Override
    public Path getBackupFolder() {
        String protocolBackupFolder = getConfigurationOption(ConfigurationConstants.CONFIGURATION_OPTION_BACKUP_FOLDER);
        return Paths.get(protocolBackupFolder);
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

}

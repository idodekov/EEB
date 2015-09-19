package org.redoubt.api.configuration;

import java.nio.file.Path;

public interface IServerConfigurationManager {
    void loadConfiguration();
    String getConfigurationOption(String name);
    void setConfigurationOption(String name, String value);
    Path getWorkFolder();
    Path getBackupFolder();
    boolean doBackup();
    int getShutDownPort();
    Path getKeystoreFile();
    String getKeystorePassword();
    Path getTruststoreFile();
    String getTruststorePassword();
    long getAs2MaxFileSizeMB();
}

package org.redoubt.api.configuration;

public interface IServerConfigurationManager {
    void loadConfiguration();
    String getConfigurationOption();
    String getTransportWorkFolder();
    String getProtocolWorkFolder();
}

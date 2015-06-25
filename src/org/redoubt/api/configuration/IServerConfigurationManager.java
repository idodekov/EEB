package org.redoubt.api.configuration;

import java.nio.file.Path;

public interface IServerConfigurationManager {
    void loadConfiguration();
    String getConfigurationOption(String name);
    void setConfigurationOption(String name, String value);
    Path getTransportWorkFolder();
    Path getProtocolWorkFolder();
    int getShutDownPort();
}

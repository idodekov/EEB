package org.redoubt.api.configuration;

import java.nio.file.Path;

public interface IServerConfigurationManager {
    void loadConfiguration();
    String getConfigurationOption();
    Path getTransportWorkFolder();
    Path getProtocolWorkFolder();
    int getShutDownPort();
}

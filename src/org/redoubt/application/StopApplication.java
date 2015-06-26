package org.redoubt.application;

import java.io.OutputStream;
import java.net.Socket;

import org.redoubt.api.configuration.IServerConfigurationManager;
import org.redoubt.api.factory.Factory;
import org.redoubt.api.factory.FactoryConstants;
import org.redoubt.application.configuration.ConfigurationConstants;
import org.redoubt.application.logging.LoggingUtils;

public class StopApplication {

    public static void main(String[] args) {
        LoggingUtils.suppressLogging();
        IServerConfigurationManager configurationManager = Factory.getInstance().getServerConfigurationManager(FactoryConstants.SERVER_CONFIGURATION_MANAGER_XML);
        int shutdownPort = configurationManager.getShutDownPort();
        Socket socket;
        try {
            socket = new Socket("127.0.0.1", shutdownPort);
            OutputStream os = socket.getOutputStream();
            os.write(ConfigurationConstants.SHUTDOWN_COMMAND.getBytes());
            os.close();
            socket.close();
        } catch (Exception e) {
            System.err.println("Error shutting down server. " + e.getMessage());
            e.printStackTrace();
        }
    }

}

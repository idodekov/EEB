package org.redoubt.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.redoubt.application.configuration.ConfigurationConstants;

public class ShutdownHook extends Thread {
    private static final Logger sLogger = Logger.getLogger(ShutdownHook.class);
    private int shutdownPort;
    
    public ShutdownHook(int shutdownPort) {
        this.shutdownPort = shutdownPort;
    }
    
    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(shutdownPort, 0, InetAddress.getByName(null));
        } catch (IOException e) {
            sLogger.error("Could not listen on shutdown port: " + shutdownPort + ". "  + e.getMessage(), e);
            return;
        }
        
        sLogger.info("Shutdown hook is listening on port " + shutdownPort);
        
        boolean shutdownReceived = false;
        
        while(!shutdownReceived) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
                
                BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String line = br.readLine();
                if(ConfigurationConstants.SHUTDOWN_COMMAND.equalsIgnoreCase(line)) {
                    sLogger.info("Received shutdown command. Stopping server...");
                    shutdownReceived = true;
                }
                
                br.close();
                clientSocket.close();
            } catch (IOException e) {
                sLogger.error("Shutdown hook could not accept connection. " + e.getMessage(), e);
                continue;
            }
        }
        
        try {
            serverSocket.close();
        } catch (IOException e) {
            sLogger.error("Error closing shutdown hook socket. " + e.getMessage(), e);
        }
    }
}

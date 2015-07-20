package org.redoubt.transport.fs;

import java.nio.file.Path;

import org.apache.log4j.Logger;
import org.redoubt.api.transport.ITransportSettings;
import org.redoubt.fs.util.FileSystemUtils;
import org.redoubt.transport.BaseTransport;
import org.redoubt.transport.TransportException;

public class FileSystemTransport extends BaseTransport {
    private static final Logger sLogger = Logger.getLogger(FileSystemTransport.class);
    private FileSystemTransportSettings fsSettings;
    private Path folder;
    private int pollingInterval;
    private FolderPollingThread pollingThread;

    @Override
    public void init(ITransportSettings settings) throws TransportException {
        fsSettings = (FileSystemTransportSettings) settings;
        setRunning(false);

        String transportName = fsSettings.getName();
        sLogger.debug("Initializing settings for transport [" + transportName + "].");
        
        pollingInterval = fsSettings.getPollingInterval();
        sLogger.debug("Polling interval is [" + pollingInterval + "] seconds.");
        
        folder = fsSettings.getFolder();
        sLogger.debug("Folder is [" + folder.toString() + "].");
        
        if(!FileSystemUtils.verifyFolderPermissions(folder)) {
            throw new TransportException("Folder [" + folder.toString() + "] doesn't exist or is not a directory.");
        }
        
        if(pollingInterval < 0) {
            throw new TransportException("Polling interval [" + pollingInterval + "] is not a valid number.");
        }
        
        pollingThread = new FolderPollingThread(folder, pollingInterval); 
    }

    @Override
    public void start() throws TransportException {
        if(isRunning()) {
            sLogger.warn("File System transport [" + fsSettings.getName() + "] can't be started - it's already running.");
        } else {
            sLogger.debug("Preparing to start File System transport [" + fsSettings.getName() + "].");
    
            try {
                pollingThread.start();
                setRunning(true);
                sLogger.info("File System transport [" + fsSettings.getName() + "] is started.");
            } catch (Exception e) {
                sLogger.error("Error starting File System transport. " + e.getMessage(), e);
                throw new TransportException(e);
            }
        }

    }

    @Override
    public void stop() throws TransportException {
        if(isRunning()) {
            sLogger.debug("Preparing to stop File System transport [" + fsSettings.getName() + "].");
            
            try {
                pollingThread.stopPollingThread();
                pollingThread.interrupt();
                pollingThread.join(60000);
                pollingThread = new FolderPollingThread(folder, pollingInterval);
                setRunning(false);
                sLogger.info("File System transport [" + fsSettings.getName() + "] is stopped.");
            } catch (Exception e) {
                sLogger.error("Error stopping File System transport. " + e.getMessage(), e);
                throw new TransportException(e);
            }
        } else {
            sLogger.warn("File System transport [" + fsSettings.getName() + "] can't be stopped - it's not running.");
        }

    }

}

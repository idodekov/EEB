package org.redoubt.transport.fs;

import java.nio.file.Path;

import org.apache.log4j.Logger;

public class FolderPollingThread extends Thread {
    private static final Logger sLogger = Logger.getLogger(FolderPollingThread.class);
    private boolean isRunning;
    private int pollingInterval;
    private Path folder;
    
    public FolderPollingThread(Path folder, int pollingInterval) {
        Thread.currentThread().setName("FolderPollingThread-" + System.currentTimeMillis());
        this.isRunning = false;
        this.pollingInterval = pollingInterval;
        this.folder = folder;
    }

    @Override
    public void run() {
        isRunning = true;
        while(isRunning) {
            sLogger.debug("Polling folder [" + folder + "]...");
            
            
            try {
                Thread.sleep(pollingInterval * 1000);
            } catch (InterruptedException e) {
                sLogger.error("[" + Thread.currentThread().getName() + "] has been interrupted while sleeping. " + e.getMessage(), e);
            }
        }
    }



    public boolean isRunning() {
        return isRunning;
    }

    public void stopPollingThread() {
        isRunning = false;
    }
    
}

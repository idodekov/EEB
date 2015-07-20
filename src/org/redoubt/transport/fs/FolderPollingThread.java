package org.redoubt.transport.fs;

import org.apache.log4j.Logger;

public class FolderPollingThread extends Thread {
    private static final Logger sLogger = Logger.getLogger(FolderPollingThread.class);
    private boolean isRunning;
    private int pollingInterval;
    
    public FolderPollingThread(int pollingInterval) {
        Thread.currentThread().setName("FolderPollingThread-" + System.currentTimeMillis());
        isRunning = true;
        this.pollingInterval = pollingInterval;
    }

    @Override
    public void run() {
        while(isRunning) {
            
            
            
            
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

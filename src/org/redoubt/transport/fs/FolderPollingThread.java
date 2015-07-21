package org.redoubt.transport.fs;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.log4j.Logger;
import org.redoubt.api.protocol.IProtocol;
import org.redoubt.api.protocol.TransferContext;
import org.redoubt.fs.util.FileSystemUtils;
import org.redoubt.transport.TransportConstants;

public class FolderPollingThread extends Thread {
    private static final Logger sLogger = Logger.getLogger(FolderPollingThread.class);
    private boolean isRunning;
    private FileSystemTransportSettings fsSettings;
    private IProtocol protocol;
    
    public FolderPollingThread(FileSystemTransportSettings fsSettings, IProtocol protocol) {
        this.isRunning = false;
        this.fsSettings = fsSettings;
        this.protocol = protocol;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("FolderPollingThread-" + System.currentTimeMillis());
        isRunning = true;
        while(isRunning) {
            sLogger.debug("Polling folder [" + fsSettings.getFolder() + "]...");
            
            try {
                Files.walkFileTree(fsSettings.getFolder(), new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        if(dir.equals(fsSettings.getFolder())) {
                            return FileVisitResult.CONTINUE;
                        } else {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        sLogger.debug("File [" + file.toString() + "] will be consumed.");
                        Path workFile = FileSystemUtils.createWorkFile(); 
                        sLogger.debug("Moving file [" + file.toString() + "] to [" + workFile.toString() + "] for processing.");
                        Files.move(file, workFile);
                        
                        FileSystemUtils.backupFile(workFile);
                        
                        TransferContext context = new TransferContext();
                        context.put(TransportConstants.CONTEXT_FULL_TARGET, workFile.toString());
                        context.put(TransportConstants.CONTEXT_ORIGINAL_FILE_NAME, file.getFileName().toString());
                        
                        protocol.process(context);
                        
                        FileSystemUtils.removeWorkFile(workFile);
                        
                        sLogger.info("File [" + file.toString() + "] has been sucesfully processed.");
                        
                        return FileVisitResult.CONTINUE;
                    }

                });
            } catch (Exception e1) {
                sLogger.error("Error while listing files  inside [" + fsSettings.getFolder() + "]. " + e1.getMessage(), e1);
            }
            
            try {
                Thread.sleep(fsSettings.getPollingInterval() * 1000);
            } catch (InterruptedException e) {
                sLogger.debug("[" + Thread.currentThread().getName() + "] has been interrupted while sleeping. " + e.getMessage(), e);
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

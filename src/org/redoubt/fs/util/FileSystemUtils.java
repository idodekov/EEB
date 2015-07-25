package org.redoubt.fs.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.redoubt.api.configuration.IServerConfigurationManager;
import org.redoubt.api.factory.Factory;

public class FileSystemUtils {
    private static final Logger sLogger = Logger.getLogger(FileSystemUtils.class);
    
    private FileSystemUtils() {}
    
    public static String generateUniqueFileName() {
        return UUID.randomUUID().toString().toLowerCase();
    }
    
    public static void backupFile(Path file) {
        IServerConfigurationManager configManager = Factory.getInstance().getServerConfigurationManager();
        
        if(!configManager.doBackup()) {
            /* backup is not enabled */
            return;
        }
        
        Path backupFolder = configManager.getBackupFolder();
        
        try {
            Path backupFile = Paths.get(backupFolder.toString(), generateUniqueFileName());
            Files.copy(file, backupFile);
            sLogger.info("The file [" + file.toString() + "] has been backed up as [" + backupFile.toString() + "].");
        } catch(Exception e) {
            sLogger.error("An error has occured while backing up file [" + file.toString() + "]. " + e.getMessage(), e);
            return;
        }
    }
    
    public static Path createWorkFile() {
        IServerConfigurationManager configManager = Factory.getInstance().getServerConfigurationManager();
        Path workFolder = configManager.getWorkFolder();
        Path workFile = Paths.get(workFolder.toString(), FileSystemUtils.generateUniqueFileName());
        return workFile;
    }
    
    public static void removeWorkFile(Path file) {
        try {
            Files.delete(file);
            sLogger.debug("Work file [" + file.toString() + "] has been succesfully deleted.");
        } catch (IOException e) {
            sLogger.error("Error while removing work file [" + file.toString() + "]. " + e.getMessage(), e);
        }
    }
    
    public static boolean verifyFolderPermissions(Path folder) {
        if(!Files.exists(folder)) {
            return false;
        }
        
        if(!Files.isDirectory(folder)) {
            return false;
        }
        
        return true;
    }
}

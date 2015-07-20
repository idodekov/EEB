package org.redoubt.fs.util;

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
            Path backupFile = Paths.get(backupFolder.toString(), file.getFileName().toString());
            Files.copy(file, backupFile);
            sLogger.info("The file [" + file.toString() + "] has been backed up as [" + backupFile.toString() + "].");
        } catch(Exception e) {
            sLogger.error("An error has occured while backing up file [" + file.toString() + "]. " + e.getMessage(), e);
            return;
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

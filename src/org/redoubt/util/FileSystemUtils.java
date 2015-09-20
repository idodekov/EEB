package org.redoubt.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

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
    
    public static Path cloneWorkFile(Path orig) {
    	IServerConfigurationManager configManager = Factory.getInstance().getServerConfigurationManager();
        Path workFolder = configManager.getWorkFolder();
        Path workFile = Paths.get(workFolder.toString(), FileSystemUtils.generateUniqueFileName());
        
        try {
			Files.copy(orig, workFile);
			return workFile;
		} catch (IOException e) {
			sLogger.error("An error has occured while cloning work file [" + orig.toString() + 
					"] to [" + workFile.toString() + "]. " + e.getMessage(), e);
			return null;
		}
    }
    
    public static void removeWorkFile(Path file) {
        try {
            Files.delete(file);
            sLogger.debug("Work file [" + file.toString() + "] has been succesfully deleted.");
        } catch (IOException e) {
            sLogger.error("Error while removing work file [" + file.toString() + "]. " + e.getMessage(), e);
        }
    }
    
    public static void moveFile(Path origFile, Path newFile, boolean overwrite) {
        try {
        	if(overwrite) {
        		Files.move(origFile, newFile, StandardCopyOption.REPLACE_EXISTING);
        	} else {
        		Files.move(origFile, newFile);
        	}
		} catch (IOException e) {
			sLogger.error("An error has occured while moving file [" + origFile.toString() + 
					"] to [" + newFile.toString() + "]. " + e.getMessage(), e);
		}
    }
    
    public static void writeMimeMessageToFile(MimeBodyPart data, Path file) throws IOException, MessagingException {
    	Files.copy(data.getInputStream(), file);
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
    
    public static boolean checkAs2SizeRestrictions(Path workFile) throws IOException {
    	long size = Files.size(workFile);
    	IServerConfigurationManager configurationManager = Factory.getInstance().getServerConfigurationManager();
    	long maxSize = configurationManager.getAs2MaxFileSizeMB() * 1024 * 1024;
    	
    	if(size > maxSize) {
    		throw new IOException("File [" + workFile.toString() + "] is [" + size + 
    				"] bytes long, but the maximum allowed size is [" + configurationManager.getAs2MaxFileSizeMB() + 
    				"] MB. The file will not be processed.");
    	}
    	
    	return true;
    }
}

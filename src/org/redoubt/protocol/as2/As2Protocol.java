package org.redoubt.protocol.as2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.redoubt.api.configuration.IServerConfigurationManager;
import org.redoubt.api.factory.Factory;
import org.redoubt.api.protocol.TransferContext;
import org.redoubt.protocol.BaseProtocol;
import org.redoubt.protocol.ProtocolException;
import org.redoubt.transport.TransportConstants;
import org.redoubt.util.FileSystemUtils;

import javax.mail.internet.MimeMessage;

public class As2Protocol extends BaseProtocol {
    private static final Logger sLogger = Logger.getLogger(As2Protocol.class);

    @Override
    public void receive(TransferContext context) throws ProtocolException {
        As2ProtocolSettings settings = (As2ProtocolSettings) getSettings();
        
        Path productionFolder = settings.getProductionFolder();
        String fullTarget = (String) context.get(TransportConstants.CONTEXT_FULL_TARGET);
        Path destination = Paths.get(fullTarget);
        
        try {
            Files.copy(destination, Paths.get(productionFolder.toString(), destination.getFileName().toString()));
        } catch (IOException e) {
            sLogger.error("An error has occured while copying destination file [" + destination.toString() + "].", e);
        }
    }

    @Override
    public void send(TransferContext context) throws ProtocolException {
        As2ProtocolSettings settings = (As2ProtocolSettings) getSettings();
        try {
        	String fullTarget = (String) context.get(TransportConstants.CONTEXT_FULL_TARGET);
        	Path workFile = Paths.get(fullTarget);
        	
        	checkSizeRestrictions(workFile);
        	
        	As2Message message = new As2Message(settings);
        	MimeMessage body = message.generateMimeData(fullTarget);
            
            Path tempStorage = FileSystemUtils.createWorkFile();
            FileSystemUtils.writeMimeMessageToFile(body, tempStorage);
            
            HttpClientUtils.sendPostRequest(settings, tempStorage);
            FileSystemUtils.moveFile(tempStorage, workFile, true);
        }  catch (Exception e) {
            sLogger.error("An error has occured while packaging As2 message. " + e.getMessage(), e);
            throw new ProtocolException(e.getMessage(), e);
        }
        
    }
	
    private boolean checkSizeRestrictions(Path workFile) throws IOException {
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

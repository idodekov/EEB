package org.redoubt.protocol.as2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.redoubt.api.protocol.TransferContext;
import org.redoubt.protocol.BaseProtocol;
import org.redoubt.protocol.ProtocolException;
import org.redoubt.transport.TransportConstants;
import org.redoubt.util.FileSystemUtils;

import javax.mail.internet.MimeBodyPart;
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
        	
        	FileSystemUtils.checkAs2SizeRestrictions(workFile);
        	
        	As2Message message = new As2Message(settings);
        	MimeBodyPart data = message.generateMimeData(fullTarget);
            
        	Path tempStorage = FileSystemUtils.createWorkFile();
            FileSystemUtils.writeMimeMessageToFile(data, tempStorage);
            
            HttpClientUtils.sendPostRequest(settings, tempStorage, message.getHeaders());
            FileSystemUtils.moveFile(tempStorage, workFile, true);
        }  catch (Exception e) {
            sLogger.error("An error has occured while packaging As2 message. " + e.getMessage(), e);
            throw new ProtocolException(e.getMessage(), e);
        }
        
    }
	
}

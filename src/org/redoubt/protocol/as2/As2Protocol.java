package org.redoubt.protocol.as2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;

import org.apache.log4j.Logger;
import org.redoubt.api.configuration.ICertificateManager;
import org.redoubt.api.configuration.ICryptoHelper;
import org.redoubt.api.factory.Factory;
import org.redoubt.api.protocol.TransferContext;
import org.redoubt.protocol.BaseProtocol;
import org.redoubt.protocol.ProtocolException;
import org.redoubt.transport.TransportConstants;
import org.redoubt.util.FileSystemUtils;

public class As2Protocol extends BaseProtocol {
    private static final Logger sLogger = Logger.getLogger(As2Protocol.class);

    @Override
    public void receive(TransferContext context) throws ProtocolException {
        As2ProtocolSettings settings = (As2ProtocolSettings) getSettings();
        try {
	        Path productionFolder = settings.getProductionFolder();
	        String fullTarget = (String) context.get(TransportConstants.CONTEXT_FULL_TARGET);
	        Path workFile = Paths.get(fullTarget);
	        
	        FileSystemUtils.checkAs2SizeRestrictions(workFile);
	        
	        InternetHeaders headers = (InternetHeaders) context.get(TransportConstants.CONTEXT_HEADER_MAP);
	        
	        As2Message message = new As2Message(Files.readAllBytes(workFile), headers);
	        message.unpackageMessage(settings);
	        
	        message.writeMimeDataToFile(workFile);
	        
	        Path productionFile = Paths.get(productionFolder.toString(), workFile.getFileName().toString());
	        message.writeMimeDataToFile(productionFile);
        } catch (Exception e) {
            sLogger.error("An error has occured while unpackaging As2 message. " + e.getMessage(), e);
            throw new ProtocolException(e.getMessage(), e);
        }
    }
    
    @Override
    public void send(TransferContext context) throws ProtocolException {
        As2ProtocolSettings settings = (As2ProtocolSettings) getSettings();
        try {
        	String fullTarget = (String) context.get(TransportConstants.CONTEXT_FULL_TARGET);
        	Path workFile = Paths.get(fullTarget);
        	
        	FileSystemUtils.checkAs2SizeRestrictions(workFile);
        	
        	As2Message message = new As2Message(workFile, null);
        	message.packageMessage(settings);
        	message.writeMimeDataToFile(workFile);
            
            HttpClientUtils.sendPostRequest(settings, workFile, message.getHeaders());
        }  catch (Exception e) {
            sLogger.error("An error has occured while packaging As2 message. " + e.getMessage(), e);
            throw new ProtocolException(e.getMessage(), e);
        }
        
    }
	
}

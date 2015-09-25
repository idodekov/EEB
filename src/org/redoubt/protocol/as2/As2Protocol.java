package org.redoubt.protocol.as2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.mail.internet.InternetHeaders;

import org.apache.log4j.Logger;
import org.redoubt.api.protocol.TransferContext;
import org.redoubt.protocol.BaseProtocol;
import org.redoubt.protocol.ProtocolException;
import org.redoubt.protocol.as2.mdn.As2MdnMessage;
import org.redoubt.transport.TransportConstants;
import org.redoubt.util.FileSystemUtils;

public class As2Protocol extends BaseProtocol {
    private static final Logger sLogger = Logger.getLogger(As2Protocol.class);

    @Override
    public void receive(TransferContext context) throws ProtocolException {
        As2ProtocolSettings settings = (As2ProtocolSettings) getSettings();
        As2Message message = null;
        
        try {
	        Path productionFolder = settings.getProductionFolder();
	        String fullTarget = (String) context.get(TransportConstants.CONTEXT_FULL_TARGET);
	        Path workFile = Paths.get(fullTarget);
	        
	        FileSystemUtils.checkAs2SizeRestrictions(workFile);
	        
	        InternetHeaders headers = (InternetHeaders) context.get(TransportConstants.CONTEXT_HEADER_MAP);
	        
	        Boolean mdnTransfer = (Boolean) context.get(TransportConstants.CONTEXT_MDN_TRANSFER);
	        if(mdnTransfer != null && mdnTransfer) {
	        	message = new As2MdnMessage(Files.readAllBytes(workFile), headers);
	        } else {
	        	message = new As2Message(Files.readAllBytes(workFile), headers);
	        }
	        message.unpackageMessage(settings);
	        
	        message.writeMimeDataToFile(workFile);
	        
	        Path productionFile = Paths.get(productionFolder.toString(), workFile.getFileName().toString());
	        message.writeMimeDataToFile(productionFile);
	        
        } catch(ProtocolException e) {
        	sLogger.error("An error has occured while unpackaging As2 message. " + e.getMessage(), e);
        	/* Don't exit here - make sure to send negative MDN if requested */
        } catch (Exception e) {
            sLogger.error("An error has occured while unpackaging As2 message. " + e.getMessage(), e);
            throw new ProtocolException(e.getMessage(), e);
        } finally {
        	if(message != null && message.isMdnReqested()) {
        		sendMdn(message, context);
	        }
        }
    }
        
    private void sendMdn(As2Message message, TransferContext context) throws ProtocolException {
    	As2ProtocolSettings settings = (As2ProtocolSettings) getSettings();
    	
    	try {
        	As2MdnMessage mdn = new As2MdnMessage(message);
        	String mdnType = mdn.getMdnType();
        	
        	Path workFile = FileSystemUtils.createWorkFile();
        	mdn.packageMessage(settings);
        	mdn.writeMimeDataToFile(workFile);
        	
        	context.put(TransportConstants.CONTEXT_MDN_TRANSFER, Boolean.TRUE);
        	context.put(TransportConstants.CONTEXT_MDN, workFile.toString());
        	context.put(TransportConstants.CONTEXT_MDN_HEADERS, mdn.getHeaders());
        	context.put(TransportConstants.CONTEXT_MDN_TYPE, mdnType);
        	context.put(TransportConstants.CONTEXT_MDN_URL, mdn.getAsynchronousMdnUrl());
        	return;
    	} catch(Exception e) {
    		 sLogger.error("An error has occured while packaging As2 MDN message. " + e.getMessage(), e);
             throw new ProtocolException(e.getMessage(), e);
    	}
    }
    
    @Override
    public void send(TransferContext context) throws ProtocolException {
        As2ProtocolSettings settings = (As2ProtocolSettings) getSettings();
        try {
        	As2Message message = null;
        	
        	String fullTarget = (String) context.get(TransportConstants.CONTEXT_FULL_TARGET);
        	Path workFile = Paths.get(fullTarget);
        	
            FileSystemUtils.checkAs2SizeRestrictions(workFile);
            
            message = new As2Message(workFile, null);
        	message.packageMessage(settings);
        	message.writeMimeDataToFile(workFile);
        	
        	FileSystemUtils.checkAs2SizeRestrictions(workFile);
            
            HttpClientUtils.sendPostRequest(this, workFile, message.getHeaders());
        }  catch (Exception e) {
            sLogger.error("An error has occured while packaging As2 message. " + e.getMessage(), e);
            throw new ProtocolException(e.getMessage(), e);
        }
        
    }
	
}

package org.redoubt.protocol.as2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Timer;

import javax.mail.internet.InternetHeaders;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.redoubt.api.protocol.TransferContext;
import org.redoubt.application.configuration.ConfigurationConstants;
import org.redoubt.protocol.BaseProtocol;
import org.redoubt.protocol.ProtocolException;
import org.redoubt.protocol.as2.mdn.As2MdnMessage;
import org.redoubt.protocol.as2.mdn.AsynchronousMdnSender;
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
	        //TODO: think on how to resolve that this is a MDN
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
        	
    		Map<String, String> mdnHeaders = mdn.getHeaders();
        		
            FileSystemUtils.backupFile(workFile);
            	
            HttpServletResponse resp = (HttpServletResponse) context.get(TransportConstants.CONTEXT_SERVLET_RESPONSE);
            
            if(ConfigurationConstants.MDN_TYPE_SYNCHRONOUS.equals(mdnType)) {
    	        for (Map.Entry<String, String> entry : mdnHeaders.entrySet()) {
    	    		resp.setHeader(entry.getKey(), entry.getValue());
    	    	}
    	        FileSystemUtils.copyFileToStream(workFile, resp.getOutputStream());
    	        FileSystemUtils.removeWorkFile(workFile);
            } else if(ConfigurationConstants.MDN_TYPE_ASYNCHRONOUS.equals(mdnType)) {
            	Timer timer = new Timer();
            	AsynchronousMdnSender mdnSender = new AsynchronousMdnSender(workFile, mdnHeaders, mdn.getAsynchronousMdnUrl());
            	timer.schedule(mdnSender, ConfigurationConstants.MDN_ASYNCHRONOUS_DELAY);
            }
            	
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

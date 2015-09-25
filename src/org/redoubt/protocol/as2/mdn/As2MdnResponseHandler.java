package org.redoubt.protocol.as2.mdn;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.mail.internet.InternetHeaders;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.log4j.Logger;
import org.redoubt.api.protocol.IProtocol;
import org.redoubt.api.protocol.TransferContext;
import org.redoubt.protocol.as2.As2HeaderDictionary;
import org.redoubt.transport.TransportConstants;
import org.redoubt.util.FileSystemUtils;

public class As2MdnResponseHandler implements ResponseHandler<Boolean> {
	private static final Logger sLogger = Logger.getLogger(As2MdnResponseHandler.class);
	private IProtocol protocol;

	public As2MdnResponseHandler(IProtocol as2Protocol) {
		protocol = as2Protocol;
	}

	@Override
	public Boolean handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		int status = response.getStatusLine().getStatusCode();
        if (status < 200 || status > 300) {
        	throw new ClientProtocolException("Unexpected response status: " + status);
        }
        
        Path workFile = null;
        
        try {
        	boolean isThisAnAs2Message = false;
        	InternetHeaders headers = new InternetHeaders();
	        for(Header header : response.getAllHeaders()) {
	        	String name = header.getName();
	        	String value = header.getValue();
	        	
	        	if(name.equals(As2HeaderDictionary.MESSAGE_ID)) {
	        		/* Confirm that this is an MDN */
	        		isThisAnAs2Message = true;
	        	}
	        	
	        	headers.setHeader(name, value);
	        }
	        
	        if(!isThisAnAs2Message) {
	        	return false;
	        }
	        
	        InputStream in = response.getEntity().getContent();
	        
	        workFile = FileSystemUtils.createWorkFile();
	        FileSystemUtils.copyStreamToFile(in, workFile);
	        FileSystemUtils.backupFile(workFile);
	        
	        TransferContext context = new TransferContext();
	        context.put(TransportConstants.CONTEXT_FULL_TARGET, workFile.toString());
	        context.put(TransportConstants.CONTEXT_HEADER_MAP, headers);
	        context.put(TransportConstants.CONTEXT_MDN_TRANSFER, Boolean.TRUE);
	        protocol.receive(context);
        } catch(Exception e) {
        	sLogger.error("An error has occured while processing synchronous MDN. " + e.getMessage(), e);
        	return false;
        } finally {
        	if(workFile != null) {
                FileSystemUtils.backupFile(workFile);
                FileSystemUtils.removeWorkFile(workFile);
            }
        }
        
        return true;
	}

}

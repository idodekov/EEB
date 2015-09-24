package org.redoubt.protocol.as2;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Map;

import javax.mail.internet.InternetHeaders;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.redoubt.api.protocol.IProtocol;
import org.redoubt.api.protocol.TransferContext;
import org.redoubt.transport.TransportConstants;
import org.redoubt.transport.http.HttpTransportSettings;
import org.redoubt.util.FileSystemUtils;

public class As2HttpListener extends HttpServlet {
    private static final Logger sLogger = Logger.getLogger(As2HttpListener.class);
	private static final long serialVersionUID = -9086455152129582063L;
	private HttpTransportSettings settings;
	private IProtocol protocol;
	
	public As2HttpListener(HttpTransportSettings settings, IProtocol protocol) {
		super();
		this.settings = settings;
		this.protocol = protocol;
	}

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Path workFile = FileSystemUtils.createWorkFile();
        try {
            FileSystemUtils.copyStreamToFile(req.getInputStream(), workFile);
            sLogger.debug("An AS2 request has been persisted in the following file: " + workFile.toString());
        } catch(IOException e) {
            sLogger.error("An error has occured while persisting AS2 request to file system. " + e.getMessage(), e);
            throw new IOException(e.getMessage(), e);
        }
        
        FileSystemUtils.backupFile(workFile);
        
        TransferContext context = new TransferContext();
        context.put(TransportConstants.CONTEXT_FULL_TARGET, workFile.toString());
        
        InternetHeaders headersMap = new InternetHeaders();
        Enumeration<String> en = req.getHeaderNames();
        while(en.hasMoreElements()) {
        	String hdr = en.nextElement();
        	headersMap.setHeader(hdr, req.getHeader(hdr));
        }
        
        context.put(TransportConstants.CONTEXT_HEADER_MAP, headersMap);
        
        try {
            protocol.process(context);
        } catch (Exception e) {
            sLogger.error("An error has occured while processing inbound AS2 message. " + e.getMessage(), e);
            throw new IOException(e.getMessage(), e);
        } finally {
        	if(workFile != null) {
	            FileSystemUtils.backupFile(workFile);
	            FileSystemUtils.removeWorkFile(workFile);
        	}
        }
        
        prepareMdnResponse(context, resp);
        
        resp.setStatus(HttpServletResponse.SC_OK);
    }
	
    private void prepareMdnResponse(TransferContext context, HttpServletResponse resp) throws IOException {
    	Boolean mdnTransfer = (Boolean) context.get(TransportConstants.CONTEXT_MDN_TRANSFER);
    	if(mdnTransfer != null) {
    		String mdnTarget = (String) context.get(TransportConstants.CONTEXT_MDN);
			@SuppressWarnings("unchecked")
			Map<String, String> mdnHeaders = (Map<String, String>) context.get(TransportConstants.CONTEXT_MDN_HEADERS);
    		
    		for (Map.Entry<String, String> entry : mdnHeaders.entrySet()) {
    			resp.setHeader(entry.getKey(), entry.getValue());
    		}
    		
        	Path mdnFile = Paths.get(mdnTarget);
        	
        	FileSystemUtils.backupFile(mdnFile);
        	FileSystemUtils.copyFileToStream(mdnFile, resp.getOutputStream());
        	FileSystemUtils.removeWorkFile(mdnFile);
    	}
    }
	
}

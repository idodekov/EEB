package org.redoubt.protocol.as2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;

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
            Files.copy(req.getInputStream(), workFile);
            sLogger.debug("An AS2 request has been persisted in the following file: " + workFile.toString());
        } catch(IOException e) {
            sLogger.error("An error has occured while persisting AS2 request to file system. " + e.getMessage(), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
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
        	resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sLogger.error("An error has occured while processing inbound AS2 message. " + e.getMessage(), e);
            throw new IOException(e.getMessage(), e);
        } finally {
            FileSystemUtils.backupFile(workFile);
            FileSystemUtils.removeWorkFile(workFile);
        }
        
        resp.setStatus(HttpServletResponse.SC_OK);
    }
	
	
}

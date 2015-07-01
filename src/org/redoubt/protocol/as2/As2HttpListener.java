package org.redoubt.protocol.as2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.redoubt.api.configuration.IServerConfigurationManager;
import org.redoubt.api.factory.Factory;
import org.redoubt.fs.util.FileSystemUtils;
import org.redoubt.transport.http.HttpTransportSettings;

public class As2HttpListener extends HttpServlet {
    private static final Logger sLogger = Logger.getLogger(As2HttpListener.class);
	private static final long serialVersionUID = -9086455152129582063L;
	private HttpTransportSettings settings;
	
	public As2HttpListener(HttpTransportSettings settings) {
		super();
		this.settings = settings;
	}

	//@Override
    //protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    //    response.setContentType("text/html");
    //    response.setStatus(HttpServletResponse.SC_OK);
    //    response.getWriter().println("<h1>Hello from HelloServlet</h1>");
    //}

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        IServerConfigurationManager configManager = Factory.getInstance().getServerConfigurationManager();
        Path workFolder = configManager.getWorkFolder();
        Path workFile = Paths.get(workFolder.toString(), FileSystemUtils.generateUniqueFileName());
        try {
            Files.copy(req.getInputStream(), workFile);
            sLogger.debug("An AS2 request has been persisted in the following file: " + workFile.toString());
        } catch(IOException e) {
            sLogger.error("An error has occured while persisting AS2 request to file system. " + e.getMessage(), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        FileSystemUtils.backupFile(workFile);
        
        resp.setStatus(HttpServletResponse.SC_OK);
    }
	
	
}

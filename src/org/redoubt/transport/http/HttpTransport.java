package org.redoubt.transport.http;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.redoubt.api.transport.ITransportSettings;
import org.redoubt.transport.BaseTransport;
import org.redoubt.transport.TransportException;

public class HttpTransport extends BaseTransport {
	private static final Logger sLogger = Logger.getLogger(HttpTransport.class);
	private HttpTransportSettings settings;
	private Server server;
	
	@Override
	public void init(ITransportSettings settings) throws TransportException {
		this.settings = (HttpTransportSettings) settings;
		setRunning(false);
		
		int port = this.settings.getPort();
        sLogger.debug("Port is [" + port + "].");
        server = new Server(port);
        
        String contextPath = this.settings.getContextPath();
        sLogger.debug("Context path is [" + contextPath + "].");
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(contextPath);
        
        server.setHandler(context);
        
        context.addServlet(new ServletHolder(new HttpListener(this.settings)), "/");
	}

	@Override
	public void start() throws TransportException {
	    if(isRunning()) {
	        sLogger.warn("HTTP transport [" + settings.getName() + "] can't be started - it's already running.");
	    } else {
    		sLogger.debug("Preparing to start HTTP transport [" + settings.getName() + "].");
    
    		try {
    			server.start();
    			setRunning(true);
    			sLogger.info("HTTP transport [" + settings.getName() + "] is started.");
    		} catch (Exception e) {
    			sLogger.error("Error starting HTTP transport. " + e.getMessage(), e);
    			throw new TransportException(e);
    		}
	    }
	}

	@Override
	public void stop() throws TransportException {
		if(isRunning()) {
		    sLogger.debug("Preparing to stop HTTP transport [" + settings.getName() + "].");
		    
            try {
                server.stop();
                setRunning(false);
                sLogger.info("HTTP transport [" + settings.getName() + "] is stopped.");
            } catch (Exception e) {
                sLogger.error("Error stopping HTTP transport. " + e.getMessage(), e);
                throw new TransportException(e);
            }
		} else {
		    sLogger.warn("HTTP transport [" + settings.getName() + "] can't be stopped - it's not running.");
		}

	}
}

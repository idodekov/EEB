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
	
	@Override
	public void init(ITransportSettings settings) throws TransportException {
		this.settings = (HttpTransportSettings) settings;
	}

	@Override
	public void start() throws TransportException {
		sLogger.debug("Preparing to start HTTP transport [" + settings.getName() + "].");
		int port = settings.getPort();
		sLogger.debug("Port is [" + port + "].");
		Server server = new Server(port);

		String contextPath = settings.getContextPath();
		sLogger.debug("Context path is [" + contextPath + "].");
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath(contextPath);
		
		server.setHandler(context);
		
		context.addServlet(new ServletHolder(new HttpListener(settings)), "/");
		
		try {
			server.start();
			sLogger.debug("HTTP transport [" + settings.getName() + "] is started.");
		} catch (Exception e) {
			sLogger.error("Error starting HTTP transport. " + e.getMessage(), e);
			throw new TransportException(e);
		}
	}

	@Override
	public void stop() throws TransportException {
		// TODO Auto-generated method stub

	}
}

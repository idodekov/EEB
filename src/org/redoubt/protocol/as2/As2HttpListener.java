package org.redoubt.protocol.as2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
        String line;
        while((line = reader.readLine()) != null) {
            sLogger.info(line);
        }
        reader.close();
        
        resp.setStatus(HttpServletResponse.SC_OK);
    }
	
	
}

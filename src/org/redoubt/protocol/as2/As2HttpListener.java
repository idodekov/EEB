package org.redoubt.protocol.as2;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.redoubt.transport.http.HttpTransportSettings;

public class As2HttpListener extends HttpServlet {
	private static final long serialVersionUID = -9086455152129582063L;
	private HttpTransportSettings settings;
	
	public As2HttpListener(HttpTransportSettings settings) {
		super();
		this.settings = settings;
	}

	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>Hello from HelloServlet</h1>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getInputStream();
        super.doPost(req, resp);
    }
	
	
}

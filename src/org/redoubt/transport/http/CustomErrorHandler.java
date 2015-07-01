package org.redoubt.transport.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.handler.ErrorHandler;

public class CustomErrorHandler extends ErrorHandler {
    private static final Logger sLogger = Logger.getLogger(CustomErrorHandler.class);

    @Override
    protected void writeErrorPage(HttpServletRequest request, Writer writer,
            int code, String message, boolean showStacks) throws IOException {
        PrintWriter printWriter = new PrintWriter(writer);
        String errorMessage = "Error Code: " + code + ". Error Message: " + message;
        printWriter.println(errorMessage);
        
        if(sLogger.isDebugEnabled()) {
            sLogger.debug("Received a bad request. The following error is generated [" + errorMessage + "].");
        }
        
        printWriter.flush();
        printWriter.close();
    }

}

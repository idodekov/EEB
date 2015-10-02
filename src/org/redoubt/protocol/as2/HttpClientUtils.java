package org.redoubt.protocol.as2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.redoubt.api.protocol.IProtocol;
import org.redoubt.protocol.as2.mdn.As2MdnResponseHandler;

public class HttpClientUtils {
    private static final Logger sLogger = Logger.getLogger(HttpClientUtils.class);
    
    public static void sendPostRequest(IProtocol protocol, Path file, Map<String,String> headers, String url) 
    		throws Exception {
        CloseableHttpClient httpclient = null;
        HttpPost httpPost = null;
        
        try {
            httpclient = HttpClients.createDefault();
            httpPost = new HttpPost(url);
            
            if(headers != null) {
            	for (Map.Entry<String, String> entry : headers.entrySet()) {
            		httpPost.setHeader(entry.getKey(), entry.getValue());
            	}
            }
            
            ByteArrayEntity entity = new ByteArrayEntity(Files.readAllBytes(file));
            httpPost.setEntity(entity);
            
            sLogger.debug("Sending POST request to " + url);
            
            if(protocol != null) {
            	As2MdnResponseHandler responseHandler = new As2MdnResponseHandler(protocol);
                httpclient.execute(httpPost, responseHandler);
            } else {
            	httpclient.execute(httpPost);
            }
        } finally {
        	if(httpPost != null) {
        		httpPost.releaseConnection();
        	}
        	
            if(httpclient != null) {
                httpclient.close();
            }
            
            
        }
    }
    
    
}

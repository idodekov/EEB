package org.redoubt.protocol.as2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.redoubt.api.protocol.IProtocol;
import org.redoubt.protocol.as2.mdn.As2MdnResponseHandler;

public class HttpClientUtils {
    private static final Logger sLogger = Logger.getLogger(HttpClientUtils.class);
    
    public static void sendPostRequest(Path file, Map<String,String> headers, String url) 
    		throws Exception {
    	sendPostRequest(null, file, headers, null, null, url);
    }
    
    public static void sendPostRequest(IProtocol protocol, Path file, Map<String,String> headers) 
    		throws Exception {
    	As2ProtocolSettings settings = (As2ProtocolSettings) protocol.getSettings();
    	sendPostRequest(protocol, file, headers, settings.getUsername(), settings.getPassword(), settings.getUrl());
    }
    
    public static void sendPostRequest(IProtocol protocol, Path file, Map<String,String> headers, String username, String password, String url) 
    		throws Exception {
        CloseableHttpClient httpclient = null;
        HttpPost httpPost = null;
        
        try {
            CredentialsProvider credsProvider = null;
            
            if(username != null && !username.trim().isEmpty()) {
                sLogger.debug("Client authentication has been enabled - setting username and password as HTTP Basic.");
                credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(
                        new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                        new UsernamePasswordCredentials(username.trim(), password));
                
            }
            
            if(credsProvider != null) {
                httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
            } else {
                httpclient = HttpClients.createDefault();
            }
            
            httpPost = new HttpPost(url);
            
            if(headers != null) {
            	for (Map.Entry<String, String> entry : headers.entrySet()) {
            		httpPost.setHeader(entry.getKey(), entry.getValue());
            	}
            }
            
            ByteArrayEntity entity = new ByteArrayEntity(Files.readAllBytes(file));
            httpPost.setEntity(entity);
            
            if(protocol == null) {
            	As2MdnResponseHandler responseHandler = new As2MdnResponseHandler(protocol);
                Boolean response = httpclient.execute(httpPost, responseHandler);
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

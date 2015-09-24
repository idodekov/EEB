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

public class HttpClientUtils {
    private static final Logger sLogger = Logger.getLogger(HttpClientUtils.class);
    
    public static void sendPostRequest(IProtocol protocol, Path file, Map<String, String> headers) throws Exception {
        CloseableHttpClient httpclient = null;
        HttpPost httpPost = null;
        As2ProtocolSettings settings = (As2ProtocolSettings) protocol.getSettings();
        
        try {
            CredentialsProvider credsProvider = null;
            
            if(settings.getUsername() != null && !settings.getUsername().trim().isEmpty()) {
                sLogger.debug("Client authentication has been enabled - setting username and password as HTTP Basic.");
                credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(
                        new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                        new UsernamePasswordCredentials(settings.getUsername().trim(), settings.getPassword()));
                
            }
            
            if(credsProvider != null) {
                httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
            } else {
                httpclient = HttpClients.createDefault();
            }
            
            httpPost = new HttpPost(settings.getUrl());
            
            if(headers != null) {
            	for (Map.Entry<String, String> entry : headers.entrySet()) {
            		httpPost.setHeader(entry.getKey(), entry.getValue());
            	}
            }
            
            ByteArrayEntity entity=new ByteArrayEntity(Files.readAllBytes(file));
            httpPost.setEntity(entity);
            
            As2ResponseHandler responseHandler = new As2ResponseHandler(protocol);
            
            Boolean response = httpclient.execute(httpPost, responseHandler);
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

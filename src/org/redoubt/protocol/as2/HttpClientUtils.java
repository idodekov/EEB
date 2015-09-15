package org.redoubt.protocol.as2;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class HttpClientUtils {
    private static final Logger sLogger = Logger.getLogger(HttpClientUtils.class);
    
    public static void sendPostRequest(As2ProtocolSettings settings, File file) throws Exception {
        CloseableHttpClient httpclient = null;
        
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
            
            HttpPost httpPost = new HttpPost(settings.getUrl());
            
            FileBody bin = new FileBody(file);
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart(file.getName(), bin)
                    .build();
            
            httpPost.setEntity(reqEntity);
            
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
    
                @Override
                public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }
    
            };
            
            String response = httpclient.execute(httpPost, responseHandler);
        } finally {
            if(httpclient != null) {
                httpclient.close();
            }
        }
    }
}

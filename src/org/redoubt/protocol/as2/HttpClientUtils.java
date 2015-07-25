package org.redoubt.protocol.as2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpClientUtils {
    public static void sendPostRequest(As2ProtocolSettings settings) throws Exception {
        CloseableHttpClient httpclient = null;
        
        try {
            httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(settings.getUrl());
            
            if(settings.getUsername() != null && !settings.getUsername().trim().isEmpty()) {
                List<NameValuePair> nvps = new ArrayList <NameValuePair>();
                nvps.add(new BasicNameValuePair("username", settings.getUsername().trim()));
                
                if(settings.getPassword() != null && !settings.getPassword().trim().isEmpty()) {
                    nvps.add(new BasicNameValuePair("password", settings.getPassword().trim()));
                }
                
                httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            }
            
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

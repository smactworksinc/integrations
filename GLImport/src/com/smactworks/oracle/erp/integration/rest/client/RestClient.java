package com.smactworks.oracle.erp.integration.rest.client;

import com.smactworks.erp.integration.exception.AuthorizationException;
import com.smactworks.erp.integration.exception.ServiceUnavailableException;
import com.smactworks.oracle.erp.integration.util.ConfigReader;
import com.smactworks.oracle.erp.integration.util.Constants;
import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class RestClient {
  private CloseableHttpClient httpClient;
  
  private HttpGet httpGet;
  
  private HttpPost httpPost;
  
  private void reinitilize() {
    this.httpPost = new HttpPost(String.valueOf(ConfigReader.getInstance().getHostName().trim()) + "/fscmRestApi/resources/latest/erpintegrations");
    this.httpPost.addHeader("Content-Type", "application/vnd.oracle.adf.resourceitem+json");
    BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
    basicCredentialsProvider.setCredentials(AuthScope.ANY, (Credentials)new UsernamePasswordCredentials(
          ConfigReader.getInstance().getUserName().trim(), ConfigReader.getInstance().getPassword().trim()));
    this.httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider((CredentialsProvider)basicCredentialsProvider).build();
  }
  
  public JSONObject post(HttpEntity entity) throws ClientProtocolException, IOException,AuthorizationException,ServiceUnavailableException {
    reinitilize();
    httpPost.setEntity(entity);
    CloseableHttpResponse response = this.httpClient.execute((HttpUriRequest)this.httpPost);
    if (response.getStatusLine().getStatusCode() == Constants.HTTP_POST_STATUS_OK.intValue()) {
      JSONObject responsePayload = new JSONObject(EntityUtils.toString(response.getEntity()));
      return responsePayload;
    }else if(response.getStatusLine().getStatusCode()==Constants.HTTP_STATUS_UNAUTHORIZED) {
    	throw new AuthorizationException("Please check the cridentials for the user account :"+ConfigReader.getInstance().getUserName().trim()+" ,cridentials either invalid (or) expired");
    }else if(response.getStatusLine().getStatusCode()==Constants.HTTP_STATUS_SERVICE_UNAVAILABLE) {
    	throw new ServiceUnavailableException("Unable to connect to Oracle service,service might be unavailable !!");
    }
    return null;
  }
  
  public JSONObject get(String url) throws ClientProtocolException, IOException {
    reinitilize();
    this.httpGet = new HttpGet(url);
    CloseableHttpResponse response = this.httpClient.execute((HttpUriRequest)this.httpGet);
    JSONObject responsePayload = new JSONObject(EntityUtils.toString(response.getEntity()));
    return responsePayload;
  }
}


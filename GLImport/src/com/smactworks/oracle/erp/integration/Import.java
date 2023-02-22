package com.smactworks.oracle.erp.integration;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Optional;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import com.smactworks.erp.integration.exception.AuthorizationException;
import com.smactworks.erp.integration.exception.ServiceUnavailableException;
import com.smactworks.oracle.erp.integration.bean.ControlBean;

public interface Import {
  Long uploadToUCM(String paramString) throws UnsupportedCharsetException, JSONException, ClientProtocolException, IOException, AuthorizationException,ServiceUnavailableException;
  
  Long submitESSJobRequest(Long paramLong, String paramString1, String paramString2) throws UnsupportedCharsetException, ClientProtocolException, IOException, AuthorizationException,ServiceUnavailableException;
  
  String getESSJobStatus(Long paramLong1, Long paramLong2) throws ClientProtocolException, IOException;
  
  List<Long> getESSJobChildProcessIds(Long paramLong) throws ClientProtocolException, IOException;
  
  String getESSJobChildProcessStatus(List<Long> paramList) throws ClientProtocolException, IOException;
  
  void setESSLogsDownloadPath(String paramString);
  
  void writeToControlFile(ControlBean paramControlBean);
  
  Optional<ControlBean> readFromControlFile(String paramString);
}

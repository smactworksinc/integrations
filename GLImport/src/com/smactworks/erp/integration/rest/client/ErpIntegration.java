package com.smactworks.erp.integration.rest.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smactworks.erp.integration.bean.ControlBean;
import com.smactworks.erp.integration.utils.Base64Utils;
import com.smactworks.erp.integration.utils.ConfigReader;
import com.smactworks.erp.integration.utils.Constants;
import com.smactworks.erp.integration.utils.ControlFileUtil;
import com.smactworks.erp.integration.utils.FileUtils;
import com.smactworks.erp.integration.utils.LoggerUtils;
import com.smactworks.erp.integration.utils.SmtpUtils;


public class ErpIntegration {
  private static Logger logger;
  
  private CloseableHttpClient httpClient;
  
  private HttpGet httpGet;
  
  private HttpPost httpPost;
  
  private CloseableHttpResponse response;
  
  private ControlFileUtil controlFileUtil = null;
  
  private List<String> processedFileList = new ArrayList<String>();
  
  private List<String> unprocessedFileList = new ArrayList<String>();
  
  private List<String> duplicateFileList = new ArrayList<String>();
  
  private List<ControlBean> controlFileList = new ArrayList<ControlBean>();
  
  private int numberOfRetries = 0;
  
  private ConfigReader configReader = null;
  
  private boolean isFileAlreadyProcessed(String fileName) {
	  boolean isAlreadyProcessed = false;
		if (this.controlFileUtil != null) {
			if (controlFileUtil.isCSVContainsEntry(fileName)) {
				isAlreadyProcessed = true;
			}
		}
		return isAlreadyProcessed;
  }
  
  private void trackFileUploadStatus(String status, String fileName, Long processId) {
    if (status.equals("SUCCESS")) {
      this.processedFileList.add(fileName);
    } else if (status.equals("FAILURE")) {
      this.unprocessedFileList.add(fileName);
    } else if (status.equals("DUPLICATE")) {
      this.duplicateFileList.add(fileName);
    } 
    ControlBean bean = new ControlBean();
    bean.setPath(FilenameUtils.separatorsToUnix(Paths.get(fileName, new String[0]).getParent().toString()));
    bean.setFileName(Paths.get(fileName, new String[0]).getFileName().toString());
    bean.setProcessId(""+processId);
    bean.setProcessStatus(status);
    bean.setProcessedTime((new SimpleDateFormat("dd/MM/yyyy_HH_mm_ss")).format(new Date()));
    this.controlFileUtil.updateCSVFile(bean);
    this.controlFileList.add(bean);
  }
  
  private void sendUploadStatusEmail() {
    Map<String, List<String>> fileProcessMap = new HashMap<>();
    fileProcessMap.put("SUCCESS", this.processedFileList);
    fileProcessMap.put("FAILURE", this.unprocessedFileList);
    fileProcessMap.put("DUPLICATE", this.duplicateFileList);
    if (!this.processedFileList.isEmpty() || !this.unprocessedFileList.isEmpty() || !this.duplicateFileList.isEmpty()) {
      boolean isSuccess = (this.unprocessedFileList.size() < 1 && this.duplicateFileList.size() < 1);
      try {
        SmtpUtils.sendEmail(SmtpUtils.getEmailSubject(isSuccess), SmtpUtils.prepareEmailBody(fileProcessMap));
      } catch (Exception e) {
        logger.error("Unable to send Email,due to error:" + e.getMessage());
      } 
    } 
  }
  
  private void sendServerHealthEmail(String operationName) {
    if (operationName.equals("uploadFileToUCM")) {
      try {
        SmtpUtils.sendEmail(SmtpUtils.getEmailSubject(false), Constants.UCM_HEALTH_EMAIL_BODY);
      } catch (Exception e) {
        logger.error("Unable to send Email,due to error:" + e.getMessage());
      } 
    } else if (operationName.equals("submitESSJobRequest")) {
      try {
        SmtpUtils.sendEmail(SmtpUtils.getEmailSubject(false), Constants.ESS_HEALTH_EMAIL_BODY);
      } catch (Exception e) {
        logger.error("Unable to send Email,due to error:" + e.getMessage());
      } 
    } 
  }
  
  public void run() {
    String parentDirectoryPath = this.configReader.getSourcePath();
    Long documentId = null, parentProcessId = null;
    boolean isArchieved = false;
    this.controlFileUtil = new ControlFileUtil();
    if (parentDirectoryPath != null) {
      if (!this.controlFileUtil.isControlFileExist())
        this.controlFileUtil.createControlFile(); 
      byte b;
      int i;
      File[] arrayOfFile;
      for (i = (arrayOfFile = (new File(parentDirectoryPath)).listFiles()).length, b = 0; b < i; ) {
        File firstLevelChild = arrayOfFile[b];
        if (firstLevelChild.isDirectory()) {
          byte b1;
          int j;
          File[] arrayOfFile1;
          for (j = (arrayOfFile1 = firstLevelChild.listFiles()).length, b1 = 0; b1 < j; ) {
            File secondLevelChild = arrayOfFile1[b1];
            if (secondLevelChild.isFile()) {
              String fileName = Paths.get(secondLevelChild.getAbsolutePath(), new String[0]).getFileName().toString();
              String archieveFolderName = String.valueOf(secondLevelChild.getParent()) + "/" + "Archieve";
              if (isFileAlreadyProcessed(fileName)) {
                try {
                  String duplicateFileName = 
                    FileUtils.getDuplicateFilePath(secondLevelChild.getAbsolutePath());
                  logger.warn("File " + secondLevelChild.getAbsolutePath() + 
                      " is processed previously,so renaming it's name to " + duplicateFileName + 
                      " and moving to archive folder ");
                  trackFileUploadStatus("DUPLICATE", 
                      secondLevelChild.getAbsolutePath(), null);
                  FileUtils.moveFile(secondLevelChild.getAbsolutePath(), 
                      String.valueOf(archieveFolderName) + "/" + duplicateFileName);
                } catch (Exception e) {
                  isArchieved = false;
                  logger.error("Unable to move the File " + secondLevelChild.getAbsolutePath() + 
                      " to archieve folder " + archieveFolderName + ",due to error " + 
                      e.getMessage());
                } 
              } else {
                initilizeRESTClient();
                logger.info("Processing the file :" + secondLevelChild.getAbsolutePath());
                try {
                  documentId = uploadToUCM(secondLevelChild.getAbsolutePath());
                  this.numberOfRetries = 0;
                } catch (IOException e) {
                  logger.error("UCM Server is not reachable ,due to error :" + e.getMessage());
                  sendServerHealthEmail("uploadFileToUCM");
                  break;
                } 
                if (documentId != null) {
                  try {
                    parentProcessId = loadDataIntoInterfaceTables(documentId, 
                        secondLevelChild.getAbsolutePath());
                    this.numberOfRetries = 0;
                  } catch (IOException e) {
                    logger.error("Unable to Submit ESS Job,due to error :" + e.getMessage());
                    sendServerHealthEmail("submitESSJobRequest");
                    break;
                  } 
                  if (parentProcessId != null) {
                    String essJobStatus = getESSJobStatus(parentProcessId);
                    logger.info("File : " + secondLevelChild.getAbsolutePath() + 
                        " \nprocessed with process Id :" + parentProcessId + 
                        " \nWith status :" + essJobStatus);
                    if (essJobStatus.equalsIgnoreCase("SUCCEEDED")) {
                      logger.info("Parent process Id :" + parentProcessId + 
                          " is Successful ,so validating child process id's for overall execution status");
                      essJobStatus = getESSChildJobStatus(
                          downloadExportOutput(parentProcessId, archieveFolderName), 
                          parentProcessId);
                      if (essJobStatus.equalsIgnoreCase("SUCCEEDED")) {
                        trackFileUploadStatus("SUCCESS", 
                            secondLevelChild.getAbsolutePath(), parentProcessId);
                      } else {
                        trackFileUploadStatus("FAILURE", 
                            secondLevelChild.getAbsolutePath(), parentProcessId);
                      } 
                    } else {
                      logger.warn("Parent process id :" + parentProcessId + " Ended with status: " + 
                          essJobStatus);
                      trackFileUploadStatus("FAILURE", 
                          secondLevelChild.getAbsolutePath(), parentProcessId);
                    } 
                    try {
                      String archiveFileName = FileUtils.getArchiveFileName(
                          secondLevelChild.getAbsolutePath(), parentProcessId, essJobStatus);
                      isArchieved = FileUtils.moveFile(secondLevelChild.getAbsolutePath(), 
                          String.valueOf(archieveFolderName) + "/" + archiveFileName);
                      logger.warn("File " + secondLevelChild.getAbsolutePath() + 
                          " moved to archieve folder " + archieveFolderName + 
                          " Successfully !");
                    } catch (Exception e) {
                      isArchieved = false;
                      logger.error("Unable to move the File " + secondLevelChild.getAbsolutePath() + 
                          " to archieve folder " + archieveFolderName + ",due to error " + 
                          e.getMessage());
                    } 
                  } 
                } 
              } 
            } 
            b1++;
          } 
        } else {
          logger.error("Invalid source folder structure ");
          logger.error(
              " Excepted source folder structure is */Inbound/BankStatements/[location]/|\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t|_Archive\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t|_*.zip\n");
        } 
        b++;
      } 
      sendUploadStatusEmail();
    } else {
      logger.error("Invalid source folder structure ");
      logger.error(
          " Excepted source folder structure is */Inbound/BankStatements/[location]/|\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t|_Archive\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t|_*.zip\n");
    } 
  }
  
  public static void main(String[] args) {
    ErpIntegration instance = new ErpIntegration();
    instance.configReader = ConfigReader.getInstance(args[0]);
    System.setProperty("logfile.name", instance.configReader.getLoggerFilePath().trim());
    LoggerUtils.getInstance(instance.configReader.getLoggerFilePath().trim());
    logger = LogManager.getLogger(ErpIntegration.class);
    logger.info("###############File Upload Process Initiated ##################################");
    long start = System.currentTimeMillis();
    instance.run();
    long end = System.currentTimeMillis();
    logger.info("Number of Files Processed \t\t\t\t:" + (instance.processedFileList.size() + 
        instance.unprocessedFileList.size() + instance.duplicateFileList.size()));
    logger.info("Number of Files Processed(Success) \t:" + instance.processedFileList.size());
    logger.info("Number of Files Processed(Failure) \t:" + instance.unprocessedFileList.size());
    logger.info("Number of Files Processed(Duplicate) \t:" + instance.duplicateFileList.size());
    logger.info("Lapsed time :" + ((end - start) / 1000L / 60L) + " Minutes");
    logger.info("###############File Upload Process Completed ##################################");
  }
  
  public void initilizeRESTClient() {
    this.httpPost = new HttpPost(String.valueOf(this.configReader.getHostName().trim()) + "/fscmRestApi/resources/latest/erpintegrations");
    this.httpPost.addHeader("Content-Type", "application/vnd.oracle.adf.resourceitem+json");
    BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
    basicCredentialsProvider.setCredentials(AuthScope.ANY, (Credentials)new UsernamePasswordCredentials(this.configReader.getUserName().trim(), 
          this.configReader.getPassword().trim()));
    this.httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider((CredentialsProvider)basicCredentialsProvider).build();
    this.controlFileUtil = new ControlFileUtil();
  }
  
  private Long uploadToUCM(String filePath) throws IOException {
    logger.info("About to upload file " + filePath + " to UCM");
    String fileName = Paths.get(filePath, new String[0]).getFileName().toString();
    Long documentId = null;
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();
    node.put("OperationName", "uploadFileToUCM");
    node.put("DocumentContent", Base64Utils.encodeBase64(filePath));
    node.put("DocumentAccount", "fin/cashManagement/import");
    node.put("ContentType", FileUtils.getFileExtension(fileName));
    node.put("FileName", fileName);
    node.put("DocumentId", "");
    this.httpPost.setEntity((HttpEntity)new StringEntity(node.toString(), ContentType.APPLICATION_JSON));
    try {
      this.response = this.httpClient.execute((HttpUriRequest)this.httpPost);
      if (this.response.getStatusLine().getStatusCode() == 201) {
        JSONObject responseJSON = new JSONObject(EntityUtils.toString(this.response.getEntity()));
        documentId = Long.valueOf(responseJSON.getLong("DocumentId"));
        logger.info(
            "**********************************UCM upload Response***************************\n File Name:" + 
            responseJSON.getString("FileName") + "\n DocumentId:" + 
            responseJSON.getString("DocumentId"));
        logger.info("********************************************************************************");
      } 
    } catch (IOException e) {
      if (this.numberOfRetries < 3) {
        uploadToUCM(filePath);
        this.numberOfRetries++;
      } 
      logger.error("Unable to connect to UCM sever: " + this.configReader.getHostName());
      throw e;
    } 
    logger.info("File :" + filePath + " Uploaded to UCM successfully !");
    return documentId;
  }
  
  private Long loadDataIntoInterfaceTables(Long documentId, String filePath) throws IOException {
    String fileName = Paths.get(filePath, new String[0]).getFileName().toString();
    String fileExtension = FileUtils.getFileExtension(fileName);
    Long processId = null;
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();
    node.put("OperationName", "submitESSJobRequest");
    node.put("JobPackageName", "oracle/apps/ess/financials/commonModules/shared/common/interfaceLoader");
    node.put("JobDefName", "InterfaceLoaderController");
    node.put("DocumentId", documentId);
    if (fileExtension.equalsIgnoreCase("zip")) {
      node.put("ESSParameters", 
          Constants.ESS_PARAM_SPREAD_SHEET_CODE + "," + documentId + ",N,N," + fileName);
    } else if (fileExtension.equalsIgnoreCase("txt") && fileName.contains("BAI_")) {
      node.put("ESSParameters", Constants.ESS_PARAM_MT940_CODE + "," + documentId + ",N,N," + fileName);
    } else if (fileExtension.equalsIgnoreCase("txt")) {
      node.put("ESSParameters", Constants.ESS_PARAM_BIA_CODE + "," + documentId + ",N,N," + fileName);
    } 
    node.put("ReqstId", "");
    this.httpPost.setEntity((HttpEntity)new StringEntity(node.toString(), ContentType.APPLICATION_JSON));
    try {
      this.response = this.httpClient.execute((HttpUriRequest)this.httpPost);
      if (this.response.getStatusLine().getStatusCode() == 201) {
        JSONObject responseJSON = new JSONObject(EntityUtils.toString(this.response.getEntity()));
        processId = Long.valueOf(responseJSON.getLong("ReqstId"));
      } 
    } catch (IOException e) {
      logger.error("Unable to connect to ESS Server for job submission for the UCM Upload : " + documentId + 
          ",Error :" + e.getMessage());
      if (this.numberOfRetries < 3) {
        loadDataIntoInterfaceTables(documentId, filePath);
        this.numberOfRetries++;
      } 
      throw e;
    } 
    return processId;
  }
  
  private List<Long> downloadExportOutput(Long parentProcessId, String logFileDownloadPath) {
    List<Long> childProcessIds = null;
    this.httpGet = new HttpGet(String.valueOf(this.configReader.getHostName().trim()) + "/fscmRestApi/resources/latest/erpintegrations?finder=ESSJobExecutionDetailsRF;requestId=" + 
        parentProcessId + ",fileType=log");
    try {
      this.response = this.httpClient.execute((HttpUriRequest)this.httpGet);
      JSONObject responseJSON = new JSONObject(EntityUtils.toString(this.response.getEntity()));
      JSONArray items = responseJSON.getJSONArray("items");
      JSONObject item = items.getJSONObject(0);
      String documentContent = item.getString("DocumentContent");
      childProcessIds = FileUtils.getAllESSChildProcessIds(
          FileUtils.writeToFile(Base64Utils.decodeFromBase64(documentContent), parentProcessId, logFileDownloadPath));
    } catch (IOException e) {
      logger.error("Unable to extract child process details for the parent process Id " + parentProcessId + 
          ",Error:" + e.getMessage());
    } 
    return childProcessIds;
  }
  
  private String getESSChildJobStatus(List<Long> childProcessIdLst, Long parentProcessId) {
    String essJobStatus = "SUCCEEDED";
    for (Long processId : childProcessIdLst) {
      this.httpGet = new HttpGet(
          String.valueOf(this.configReader.getHostName().trim()) + "/fscmRestApi/resources/latest/erpintegrations?finder=ESSJobStatusRF;requestId=" + processId);
      try {
        this.response = this.httpClient.execute((HttpUriRequest)this.httpGet);
        JSONObject responseJSON = new JSONObject(EntityUtils.toString(this.response.getEntity()));
        JSONArray items = responseJSON.getJSONArray("items");
        JSONObject item = items.getJSONObject(0);
        String requestStatus = item.getString("RequestStatus");
        if (requestStatus.equalsIgnoreCase("ERROR")) {
          logger.warn("Child process Id :" + processId + " is :" + requestStatus);
          return "ERROR";
        } 
        essJobStatus = requestStatus;
      } catch (IOException e) {
        logger.error("Unable to get the child process status,Error" + e.getMessage());
      } 
    } 
    return essJobStatus;
  }
  
  private String getESSJobStatus(Long requestId) {
    boolean isCompleted = false;
    String essJobStatus = "";
    int counter = 0;
    while (!isCompleted) {
      try {
        logger.info("Waiting for 30 Seconds for getting ESS Job Status ");
        Thread.sleep(30000L);
        this.httpGet = new HttpGet(
            String.valueOf(this.configReader.getHostName().trim()) + "/fscmRestApi/resources/latest/erpintegrations?finder=ESSJobStatusRF;requestId=" + requestId);
        this.response = this.httpClient.execute((HttpUriRequest)this.httpGet);
        JSONObject responseJSON = new JSONObject(EntityUtils.toString(this.response.getEntity()));
        JSONArray items = responseJSON.getJSONArray("items");
        JSONObject item = items.getJSONObject(0);
        String requestStatus = item.getString("RequestStatus");
        counter++;
        if (requestStatus.equals("SUCCEEDED") || 
          requestStatus.equals("ERROR")) {
          essJobStatus = requestStatus;
          isCompleted = true;
        } else {
          logger.info("ESS Process: " + requestId + " is in :" + requestStatus + 
              " state ,will verify the status again in 30 seconds ");
        } 
        if (counter > 4)
          isCompleted = true; 
      } catch (InterruptedException e) {
        logger.error("Unable to wait for 1 minute for getting ESS Job Status");
      } catch (ClientProtocolException e) {
        logger.error("Unable get the ESS Job Status Client Protocol Error");
      } catch (IOException e) {
        logger.error("Unable get the ESS Job Status IO Error");
      } 
    } 
    return essJobStatus;
  }
}

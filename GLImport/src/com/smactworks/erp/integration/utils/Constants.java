package com.smactworks.erp.integration.utils;

public class Constants {
  public static final String PROCESS_SUCCESS = "SUCCEEDED";
  
  public static final String PROCESS_FAILED = "FAILED";
  
  public static final String PROCESS_ABORTED = "ABORTED";
  
  public static final String PROCESS_WAIT = "WAIT";
  
  public static final String FSCM_REST_API = "/fscmRestApi/resources/latest/erpintegrations";
  
  public static final String ESS_JOB_STATUS_REST_API = "/fscmRestApi/resources/latest/erpintegrations?finder=ESSJobStatusRF;requestId=";
  
  public static final String ESS_JOB_LOG_DOWNLOAD_REST_API = "/fscmRestApi/resources/latest/erpintegrations?finder=ESSJobExecutionDetailsRF;requestId=";
  
  public static final String DOCUMENT_ACCOUNT = "fin/cashManagement/import";
  
  public static final String OPERATION_UCM_UPLOAD = "uploadFileToUCM";
  
  public static final String OPERATION_SUBMIT_ESS = "submitESSJobRequest";
  
  public static final String OPERATION_INTERFACE_CONTROLER = "InterfaceLoaderController";
  
  public static final String ATTRIBUTE_DOCUMENT_ID = "DocumentId";
  
  public static final String ATTRIBUTE_REQUEST_ID = "ReqstId";
  
  public static final String ATTRIBUTE_REQUEST_STATUS = "RequestStatus";
  
  public static final String JOB_PACKAGE_NAME_INTERFACE_LOADER = "oracle/apps/ess/financials/commonModules/shared/common/interfaceLoader";
  
  public static final String DUPLICATE_FILE_SUFFIX = "-Duplicate";
  
  public static final String ARCHIEVE_FOLD_NAME = "Archieve";
  
  public static final String ESS_JOB_STATUS_SUCCESS = "SUCCEEDED";
  
  public static final String ESS_JOB_STATUS_FAILURE = "ERROR";
  
  public static final String FILE_PROCESS_SUCCESS = "SUCCESS";
  
  public static final String FILE_PROCESS_FAILURE = "FAILURE";
  
  public static final String FILE_PROCESS_DUPLICATE = "DUPLICATE";
  
  public static final Integer ESS_PARAM_SPREAD_SHEET_CODE = Integer.valueOf(117);
  
  public static final Integer ESS_PARAM_MT940_CODE = Integer.valueOf(117);
  
  public static final Integer ESS_PARAM_BIA_CODE = Integer.valueOf(117);
  
  public static final String ESS_PARAMETERS = "ESSParameters";
  
  public static final String FILE_EXTENSION_ZIP = "zip";
  
  public static final String FILE_EXTENSION_TXT = "txt";
  
  public static final StringBuilder UCM_HEALTH_EMAIL_BODY = new StringBuilder("<p>Hello,</p>\n Unable to connect to UCM server ,file upload process is terminated");
  
  public static final StringBuilder ESS_HEALTH_EMAIL_BODY = new StringBuilder("<p>Hello,</p>\\n Unable to submit ESS Job,file upload process is terminated");
  
  public static final String HTML_BODY_FILE_NAME = "emailHtmlBody.txt";
}

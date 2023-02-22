package com.smactworks.oracle.erp.integration.util;

import java.util.HashMap;
import java.util.Map;

public class Constants {
  public static final String CONTROL_FILE_HEADER_PATH = "path";
  
  public static final String CONTROL_FILE_HEADER_FILE_NAME = "fileName";
  
  public static final String CONTROL_FILE_HEADER_PROCESS_ID = "processId";
  
  public static final String CONTROL_FILE_HEADER_PROCESS_STATUS = "processStatus";
  
  public static final String CONTROL_FILE_HEADER_PROCESS_TIME = "processedTime";
  
  public static final String[] CONTROL_FILE_HEADER_MAP = new String[] { "path", "fileName", 
      "processId", "processStatus","fileStamp", "processedTime" };
  
  public static final String FILE_EXTENSION_CSV = ".csv";
  
  public static final String FILE_EXTENSION_ZIP = ".csv";
  
  public static final String DATE_MONTH_FORMAT = "MMM";
  
  public static final String DATE_YEAR_FORMAT = "YYYY";
  
  public static final String DATE_TIME_FORMAT = "MMddYYYY";
  
  public static final char SEPERATOR_COMMA = ',';
  
  public static final char SEPERATOR_DOT = '.';
  
  public static final String SEPERATOR_SPACE = " ";
  
  public static final String SEPERATOR_BACKWARD_SLASH = "/";
  
  public static final String SEPERATOR_FORWARD_SLASH = "";
  
  public static final String FSCM_REST_API = "/fscmRestApi/resources/latest/erpintegrations";
  
  public static final String FSCM_REST_STATUS_API = "/fscmRestApi/resources/latest/erpintegrations?finder=ESSJobStatusRF;requestId=";
  
  public static final String FSCM_REST_PROCESS_LOG_DOWNLOAD_API = "/fscmRestApi/resources/latest/erpintegrations?finder=ESSJobExecutionDetailsRF;requestId=";
  
  public static final String CONFIG_PROPERTY_HOST_NAME = "hostName";
  
  public static final String CONFIG_PROPERTY_USER_NAME = "userName";
  
  public static final String CONFIG_PROPERTY_PASSWORD = "password";
  
  public static final String CONFIG_PROPERTY_INPUT_FILE_PATH = "inputFilePath";
  
  public static final String CONFIG_PROPERTY_ARCHIVE_FILE_PATH = "archieveFilePath";
  
  public static final String CONFIG_PROPERTY_LOG_FILE_PATH = "logFileLocation";
  
  public static final String CONFIG_PROPERTY_CONTROL_FILE_PATH = "controlFileLocation";
  
  public static final String CONFIG_PROPERTY_JOB_PACKAGE_NAME = "jobPackageName";
  
  public static final String CONFIG_PROPERTY_CSV_JOB_DEF_NAME = "csvJobDefName";
  
  public static final String CONFIG_PROPERTY_SMTP_HOST = "smtpServer";
  
  public static final String CONFIG_PROPERTY_SMTP_PORT = "smtpPort";
  
  public static final String CONFIG_PROPERTY_EMAIL_FROM = "emailFrom";
  
  public static final String CONFIG_PROPERTY_EMAIL_TO = "emailTo";
  
  public static final String CONFIG_PROPERTY_SMTP_AUTH_USER = "smtpAuthUserName";
  
  public static final String CONFIG_PROPERTY_SMTP_AUTH_PWD = "smtpAuthPassword";
  
  public static final String CONFIG_PROPERTY_EMAIL_SUBJECT_SUCCESS = "emailSubjectProcessSuccess";
  
  public static final String CONFIG_PROPERTY_EMAIL_SUBJECT_FAILURE = "emailSubjectProcessFailure";
  
  public static final String CONFIG_PROPERTY_EMAIL_SUBJECT_UCM_DOWN = "emailSubjectUCMDown";
  
  public static final String CONFIG_PROPERTY_EMAIL_SUBJECT_ESS_DOWN = "emailSubjectESSDown";
  
  public static final String CONFIG_PROPERTY_EMAIL_BODY = "emailBody";
  
  public static final String CONFIG_PROPERTY_RUN_ENV = "runEnv";
  
  public static final String HTTP_HEADER_CONTENT_TYPE = "application/vnd.oracle.adf.resourceitem+json";
  
  public static final String SYSTEM_PROPERTY_LOGGER = "logfile.name";
  
  public static final Integer HTTP_STATUS_OK = Integer.valueOf(200);
  
  public static final Integer HTTP_POST_STATUS_OK = Integer.valueOf(201);
  
  public static final Integer HTTP_STATUS_UNAUTHORIZED=Integer.valueOf(403);
  
  public static final Integer HTTP_STATUS_SERVICE_UNAVAILABLE=Integer.valueOf(500);
  
  public static final String GL_IMPORT_SECURITY_ACCOUNT = "fin$/generalLedger$/import$";
  
  public static final String UCM_UPLOAD_SECURITY_ACCOUNT = "fin$/journal$/import$";
  
  public static final String GL_IMPORT_SECURITY_GROUP = "FAFusionImportExport";
  
  public static final String INTERFACE_UPLOADER_ESS_JOB_PACKAGE = "oracle/apps/ess/financials/commonModules/shared/common/interfaceLoader";
  
  public static final String GL_IMPORT_ESS_JOB_PACKAGE = "/oracle/apps/ess/financials/generalLedger/programs/common";
  
  public static final String INTERFACE_LOADER_ESS_JOB_DEF = "InterfaceLoaderController";
  
  public static final String GL_IMPORT_ESS_JOB_DEF = "JournalImportLauncher";
  
  public static final String OPERATION_UCM_UPLOAD = "uploadFileToUCM";
  
  public static final String OPERATION_SUBMIT_ESS = "submitESSJobRequest";
  
  public static final String GL_UCM_UPLOAD_CONTENT_TYPE = "csv";
  
  public static final String HTML_BODY_FILE_NAME = "emailHtmlBody.txt";
  
  public static final String DUPLICATE_FILE_SUFFIX = "-Duplicate";
  
  public static final String ARCHIEVE_FOLD_NAME = "Archieve";
  
  public static final String FILE_PROCESS_SUCCESS = "Success";
  
  public static final String FILE_PROCESS_FAILURE = "Error";
  
  public static final String FILE_PROCESS_DUPLICATE = "Duplicate";
  
  public static final String ESS_JOB_STATUS_SUCCESS = "SUCCEEDED";
  
  public static final String ESS_JOB_STATUS_FAILURE = "ERROR";
  
  public static final String UCM_UPLOAD_RESPONSE_DOC_ID = "DocumentId";
  
  public static final String ESS_JOB_RESPONSE_PROCESS_ID = "ReqstId";
  
  public static final String ESS_JOB_RESPONSE_REQUEST_STATUS = "RequestStatus";
  
  public static final String ESS_JOB_LEDGER_IMPORT = "InterfaceLoaderController";
  
  public static final String ESS_JOB_JOURNAL_IMPORT = "JournalImportLauncher";
  public static final String ESS_JOB_PARAM_ITEMS = "items";
  
  public static final String ESS_PROFILE_UAT = "UAT";
  public static final String ESS_PROFILE_PRD = "PRD";
  public static final String ESS_PROFILE_UAT_ACC_ID = "300000001510118,300000002289095,";
  public static final String ESS_PROFILE_PRD_ACC_ID = "300000002485139,300000002485140,";
  
  public static Map<String, Long> UAT_JOURNAL_LEDGER_MAP = new HashMap<>();
  
  public static Map<String, Long> PRD_JOURNAL_LEDGER_MAP = new HashMap<>();
  
  public static void loadLeaders() {
    UAT_JOURNAL_LEDGER_MAP.put("JMMBJM", Long.valueOf(300000001423045L));
    UAT_JOURNAL_LEDGER_MAP.put("JMMBDO", Long.valueOf(300000001423046L));
    UAT_JOURNAL_LEDGER_MAP.put("JMMBTT", Long.valueOf(300000001423047L));
    UAT_JOURNAL_LEDGER_MAP.put("JMMBUS", Long.valueOf(300000001423048L));
    PRD_JOURNAL_LEDGER_MAP.put("JMMBJM", Long.valueOf(300000002487036L));
    PRD_JOURNAL_LEDGER_MAP.put("JMMBDO", Long.valueOf(300000002487037L));
    PRD_JOURNAL_LEDGER_MAP.put("JMMBTT", Long.valueOf(300000002487038L));
    PRD_JOURNAL_LEDGER_MAP.put("JMMBUS", Long.valueOf(300000002487452L));
  }
}

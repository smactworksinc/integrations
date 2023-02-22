package com.smactworks.oracle.erp.integration.execute;

import java.io.File;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.smactworks.erp.integration.exception.AuthorizationException;
import com.smactworks.erp.integration.exception.ServiceUnavailableException;
import com.smactworks.oracle.erp.integration.Import;
import com.smactworks.oracle.erp.integration.bean.ControlBean;
import com.smactworks.oracle.erp.integration.impl.GLImport;
import com.smactworks.oracle.erp.integration.util.ConfigReader;
import com.smactworks.oracle.erp.integration.util.Constants;
import com.smactworks.oracle.erp.integration.util.ControlFileUtil;
import com.smactworks.oracle.erp.integration.util.FileUtils;
import com.smactworks.oracle.erp.integration.util.LoggerUtils;
import com.smactworks.oracle.erp.integration.util.SmtpUtils;

public class ErpIntegrationClient {
  private static Logger logger;
  
  private static ConfigReader configReader = null;
  
  private static List<String> processedFileList = new ArrayList<String>(),unprocessedFileList = new ArrayList<String>(),duplicateFileList = new ArrayList<String>();
  
  private static Import instance;
  
  private static Map<String, Long> processIdMap = new HashMap<>();
  
  private static void initilize(Optional<String> configFilePath) {
	  configReader = ConfigReader.getInstance(configFilePath.get());
	  System.setProperty("logfile.name", configReader.getLoggerFilePath().trim());
	  LoggerUtils.getInstance(configReader.getLoggerFilePath().trim());
	  logger = LogManager.getLogger(ErpIntegrationClient.class);
  }
  
  public static void main(String[] args) {
	  if(args==null || args.length==0) {
		  // Quit the process
		  System.out.println("Please provide the configuration file path");
		  return;
	  }
	initilize(Optional.of(args[0]));
	logger.info("############################File upload process is initated##############################");
    boolean iscompleted=processFiles();
    if(iscompleted)
    	logger.info("############################File uploading process completed################################");
    else
    	logger.warn("############################File uploading process intrupted################################");
  }
  
  private static ControlBean initilizeControlBean(String fileName, String processId, String status) {
	  return ControlBean.builder()
			  .path(Optional.of(FilenameUtils.separatorsToUnix(Paths.get(fileName, new String[0]).getParent().toString())))
			  .fileName(Optional.of(Paths.get(fileName, new String[0]).getFileName().toString()))
			  .processedTime(Optional.of((new SimpleDateFormat(Constants.DATE_TIME_FORMAT)).format(new Date())))
			  .processId(Optional.of(processId))
			  .processStatus(Optional.of(status))
			  .fileStamp(FileUtils.getMD5Checksum(new File(fileName)))
			  .build();
  }
  
  private static boolean isValidSourcePath(Optional<String> filePath) {
	  return (!filePath.isPresent() || new File(filePath.get()).isDirectory())?false:true;
  }
  
  private static void markFileAsDuplicate(File file) {
	logger.info("Begin Mark file status to Duplicate");
    String dupilcateFileName = FileUtils.getDuplicateFilePath(file.getAbsolutePath());
    try {
    	logger.info("Checksum of processed file:"+file.getAbsolutePath()+" is:"+FileUtils.getMD5Checksum(file));
      instance.writeToControlFile(
    	          initilizeControlBean(file.getAbsolutePath(), Constants.FILE_PROCESS_DUPLICATE, null));
      FileUtils.moveFile(file.getAbsolutePath(), String.valueOf(ConfigReader.getInstance().getArchiveFilePath()) + 
          "/" + dupilcateFileName);
      duplicateFileList.add(file.getAbsolutePath());
      
      processIdMap.put(file.getAbsolutePath(), null);
      
    } catch (IOException e) {
      logger.error("Error While moving the duplicate file to archieve folder"+e.getMessage());
    }
    logger.info("End Mark file status to duplicate");
  }
  
  private static void markFileAsProcessed(File file, String status, Long processId) {
	  logger.info("Begin mark the file status as processed");
    String archiveFileName = FileUtils.getArchiveFileName(file.getAbsolutePath(), processId, status);
    try {
    	logger.info("Checksum of processed file:"+file.getAbsolutePath()+" is:"+FileUtils.getMD5Checksum(file));
      instance.writeToControlFile(initilizeControlBean(file.getAbsolutePath(), status, processId.toString()));
      FileUtils.moveFile(file.getAbsolutePath(), String.valueOf(ConfigReader.getInstance().getArchiveFilePath()) + 
          "/" + archiveFileName);
      if (status.equalsIgnoreCase(Constants.FILE_PROCESS_SUCCESS)) {
        processedFileList.add(file.getAbsolutePath());
      } else if (status.equalsIgnoreCase(Constants.FILE_PROCESS_FAILURE)) {
        unprocessedFileList.add(file.getAbsolutePath());
      } 
      
      processIdMap.put(file.getAbsolutePath(), processId);
    } catch (IOException e) {
      logger.error("Error while moving processed file to archieve folder :"+e.getMessage());
    } 
    logger.info("End mark file status as processed");
  }
  
  private static boolean moveFilesToStagging(Path path) {
	  logger.info("Begin moveFilesToStagging");
	  path.forEach(file->{
		try {
			Files.move(file.getFileName(),Paths.get(ConfigReader.getInstance().getStaggingFilePath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	});
	  logger.info("End of moveFilesToStagging");
	  return true;
	  
  }
  
  private static boolean processFiles() {
    Optional<String> inputSourcePath = Optional.of(configReader.getSourcePath());
    moveFilesToStagging(Paths.get(inputSourcePath.get()));
    inputSourcePath=Optional.of(configReader.getStaggingFilePath());
    if (isValidSourcePath(inputSourcePath)) {
      File parentDirectory = new File(inputSourcePath.get());
      instance = new GLImport();
      ControlFileUtil controlFileUtil = new ControlFileUtil();
      for(File file:parentDirectory.listFiles()) {
        logger.info("Processing started for the the file :" + file.getName());
        if (controlFileUtil.readFromControlFile(file.getName()).isPresent()) {
          logger.warn("File :" + file.getName() + 
              " is already processed in the previous attempts so marking it as duplicate");
          markFileAsDuplicate(file);
        } else {
          try {
            logger.info("Calling Upload file to UCM process for :" + file.getAbsolutePath());
            Long documentId = instance.uploadToUCM(file.getAbsolutePath());
            logger.info("Successfully Upload file to UCM !");
            
            logger.info("Calling Load file to interface job");
            Long processId = instance.submitESSJobRequest(documentId, "InterfaceLoaderController", 
                file.getName());
            Long parentProcessId = processId;
            String status = instance.getESSJobStatus(processId, Long.valueOf(20000L));
            
            
            
            if (status.equalsIgnoreCase(Constants.ESS_JOB_STATUS_SUCCESS)) {
              logger.info("Load file to interface job completed Successfully ! ");
              instance.setESSLogsDownloadPath(String.valueOf(ConfigReader.getInstance().getArchiveFilePath()) + "/");
              String childProcessStatus = instance
                .getESSJobChildProcessStatus(instance.getESSJobChildProcessIds(processId));
              
              
              if (childProcessStatus.equalsIgnoreCase(Constants.ESS_JOB_STATUS_SUCCESS)) {
                logger.info("Calling Import Journals Job");
                processId = instance.submitESSJobRequest(documentId, "JournalImportLauncher", 
                    file.getName());
                status = instance.getESSJobStatus(processId, Long.valueOf(20000L));
                
                if (status.equalsIgnoreCase(Constants.ESS_JOB_STATUS_SUCCESS)) {
                  childProcessStatus = instance
                    .getESSJobChildProcessStatus(instance.getESSJobChildProcessIds(processId));
                  
	                  if (childProcessStatus.equalsIgnoreCase(Constants.ESS_JOB_STATUS_SUCCESS)) {
	                    logger.info("Import Journals Job completed Successfully !");
	                    markFileAsProcessed(file, Constants.FILE_PROCESS_SUCCESS, parentProcessId);
	                  } else {
	                    logger.info("Journal Import process ended with status :" + status);
	                    markFileAsProcessed(file, Constants.FILE_PROCESS_FAILURE, parentProcessId);
	                  } 
                  
                } else {
                  logger.info("Load file to interface job failed with ESS Job status : " + status);
                  markFileAsProcessed(file, Constants.FILE_PROCESS_FAILURE, parentProcessId);
                } 
              }else { 
		              logger.info("Writing file process status into control file");
		              markFileAsProcessed(file, childProcessStatus, parentProcessId);
              }
            } else {
              logger.info("Load file to interface job failed with ESS Job status : " + status);
              markFileAsProcessed(file, Constants.FILE_PROCESS_FAILURE, parentProcessId);
            } 
          } catch (UnsupportedCharsetException|org.json.JSONException|IOException e) {
        	  sendHealthEmail(Optional.of(e.getMessage()));
            logger.error("Unable to upload file to UCM,due to error =>" + e.getMessage());
          } catch(AuthorizationException e) {
        	  sendHealthEmail(Optional.of(e.getMessage()));
        	  return false;
          }catch(ServiceUnavailableException e) {
        	  sendHealthEmail(Optional.of(e.getMessage()));
        	  return false;
          }
        }
        logger.info("File :"+file.getName()+" processed successfully");
      } 
      sendUploadStatusEmail();
    } else {
      logger.warn("Provided input source path is invalid ,terminating the process");
      return false;
    } 
    return true;
  }
  
  private static void sendHealthEmail(Optional<String> message) {
	  Map<String, List<String>> fileProcessMap = new HashMap<>();
	    fileProcessMap.put(Constants.FILE_PROCESS_FAILURE, unprocessedFileList);
	    try {
	        SmtpUtils.sendEmailUsingSMTP(ConfigReader.getInstance().getEmailSubjectFailure(), 
	            SmtpUtils.prepareHealthCheckEmailBody(message.get()));
	      } catch (Exception e) {
	        logger.error("Unable to send Email,due to error:" + e.getMessage());
	      }  
  }
  
  private static void sendUploadStatusEmail() {
    Map<String, List<String>> fileProcessMap = new HashMap<>();
    fileProcessMap.put(Constants.FILE_PROCESS_SUCCESS, processedFileList);
    fileProcessMap.put(Constants.FILE_PROCESS_FAILURE, unprocessedFileList);
    fileProcessMap.put(Constants.FILE_PROCESS_DUPLICATE, duplicateFileList);
    if (!processedFileList.isEmpty() || !unprocessedFileList.isEmpty() || !duplicateFileList.isEmpty())
      try {
        SmtpUtils.sendEmailUsingSMTP(
            SmtpUtils.getEmailSubject(
              (unprocessedFileList.size() < 1 && duplicateFileList.size() < 1)), 
            SmtpUtils.prepareEmailBody(fileProcessMap, processIdMap));
      } catch (Exception e) {
        logger.error("Unable to send Email,due to error:" + e.getMessage());
      }  
  }
}

package com.smactworks.oracle.erp.integration.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.Getter;

public class ConfigReader {
  private Properties properties;
  
  private static ConfigReader configReader;
  
  private ConfigReader(String propertyFilePath) {
    InputStream input = null;
    try {
      input = new FileInputStream(propertyFilePath);
      this.properties = new Properties();
      this.properties.load(input);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        input.close();
      } catch (IOException e) {
        e.printStackTrace();
      } 
    } 
  }
  
  public static ConfigReader getInstance() {
    if (configReader != null)
      return configReader; 
    return null;
  }
  
  public static ConfigReader getInstance(String propertyFilePath) {
    if (configReader != null)
      return configReader; 
    configReader = new ConfigReader(propertyFilePath);
    return configReader;
  }
  
  public String getHostName() {
    return this.properties.getProperty("hostName");
  }
  
  public String getUserName() {
    return this.properties.getProperty("userName");
  }
  
  public String getPassword() {
    return this.properties.getProperty("password");
  }
  
  public String getSourcePath() {
    return this.properties.getProperty("inputFilePath");
  }
  
  public String getArchiveFilePath() {
    return this.properties.getProperty("archieveFilePath");
  }
  
  public String getStaggingFilePath() {
	    return this.properties.getProperty("staggingFilePath");
  }
  
  public String getLoggerFilePath() {
    return this.properties.getProperty("logFileLocation");
  }
  
  public String getControlFilePath() {
    return this.properties.getProperty("controlFileLocation");
  }
  
  public String getJobPackageName() {
    return this.properties.getProperty("jobPackageName");
  }
  
  public String getCSVJobDefName() {
    return this.properties.getProperty("csvJobDefName");
  }
  
  public String getSmtpServer() {
    return this.properties.getProperty("smtpServer");
  }
  
  public String getSmtpPort() {
    return this.properties.getProperty("smtpPort");
  }
  
  public String getEmailFrom() {
    return this.properties.getProperty("emailFrom");
  }
  
  public String getEmailTo() {
    return this.properties.getProperty("emailTo");
  }
  
  public String getSmtpAuthUserName() {
    return this.properties.getProperty("smtpAuthUserName");
  }
  
  public String getSmtpAuthPassword() {
    return this.properties.getProperty("smtpAuthPassword");
  }
  
  public String getEmailSubjectSuccess() {
    return this.properties.getProperty("emailSubjectProcessSuccess");
  }
  
  public String getEmailSubjectFailure() {
    return this.properties.getProperty("emailSubjectProcessFailure");
  }
  
  public String getEmailSubjectUCMDown() {
    return this.properties.getProperty("emailSubjectUCMDown");
  }
  
  public String getEmailSubjectESSDown() {
    return this.properties.getProperty("emailSubjectESSDown");
  }
  
  public String getEmailBody() {
    return this.properties.getProperty("emailBody");
  }
  
  public String getRunEnv() {
    return this.properties.getProperty("runEnv");
  }
}


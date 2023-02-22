package com.smactworks.erp.integration.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.PropertyConfigurator;

public class LoggerUtils {
  private static LoggerUtils loggerUtils;
  
  public static LoggerUtils getInstance(String logFile) {
    if (loggerUtils != null)
      return loggerUtils; 
    loggerUtils = new LoggerUtils();
    loggerUtils.updateLog4jConfiguration(logFile);
    return loggerUtils;
  }
  
  public void updateLog4jConfiguration(String logFile) {
    System.out.println(logFile);
    Properties props = new Properties();
    try {
      InputStream configStream = getClass().getClassLoader().getResourceAsStream("log4j.properties");
      props.load(configStream);
      configStream.close();
    } catch (IOException e) {
      System.out.println("Errornot laod configuration file ");
    } 
    props.setProperty("log4j.appender.file.File", logFile);
    props.setProperty("log4j.appender.Appender2.File", logFile);
    PropertyConfigurator.configure(props);
  }
}

package com.smactworks.erp.integration.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.MappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.smactworks.erp.integration.bean.ControlBean;

public class ControlFileUtil {
  private static final String[] header = new String[] { "path", "fileName", "processId", "processStatus", "processedTime" };
  
  private static final Logger logger = LogManager.getLogger(ControlFileUtil.class);
  
  private String getCurrentMonthName() {
    SimpleDateFormat f = new SimpleDateFormat("MMM");
    return f.format(new Date());
  }
  
  private String getCurrentYear() {
    SimpleDateFormat f = new SimpleDateFormat("YYYY");
    return f.format(new Date());
  }
  
  private String prepareControlFileName() {
    return String.valueOf(ConfigReader.getInstance().getControlFilePath().trim()) + "/" + getCurrentMonthName() + " " + 
      getCurrentYear() + ".csv";
  }
  
  public ControlFileUtil() {
    LoggerUtils.getInstance(ConfigReader.getInstance().getLoggerFilePath().trim());
  }
  
  public boolean isControlFileExist() {
    String controlFileName = prepareControlFileName();
    logger.info("Checking whether the control file " + controlFileName + "existence ");
    boolean isControlFileExist = true;
    if (!(new File(controlFileName)).exists()) {
      isControlFileExist = false;
      logger.info("Control file is not exist ");
    } 
    return isControlFileExist;
  }
  
  public boolean createControlFile() {
	  
	  String controlFileName=prepareControlFileName();
		logger.info("About to creat Control file ");
		boolean controlFileCreated = false;
		List<String[]> list = new ArrayList<>();
		list.add(header);
		try (CSVWriter writer = new CSVWriter(new FileWriter(controlFileName))) {
			writer.writeAll(list);
			logger.info("Control file : " + controlFileName + " created successfully !");
		} catch (IOException e) {
			logger.error("Unable to creat control file due to error :" + e.getMessage());
		}
		return controlFileCreated;
	  
	  
    /*String controlFileName = prepareControlFileName();
    logger.info("About to creat Control file ");
    boolean controlFileCreated = false;
    List<String[]> list = (List)new ArrayList<>();
    list.add(header);
    try {
      Exception exception2, exception1 = null;
    } catch (IOException e) {
      logger.error("Unable to creat control file due to error :" + e.getMessage());
    } 
    return controlFileCreated;*/
  }
  
  public boolean isFileAlreadyProcessed(String value) {
    boolean isPresent = false;
    List<ControlBean> beans = csvToBean();
    for (ControlBean bean : beans) {
      logger.info("Files in csv " + bean.getFileName());
      if (bean.getFileName() != null && bean.getFileName().equals(value)) {
        isPresent = true;
        logger.info(
            "File: " + value + " ,processed in previous execution with process Id: " + bean.getProcessId() + 
            " ,on " + bean.getProcessedTime() + " with status:" + bean.getProcessStatus());
        break;
      } 
    } 
    return isPresent;
  }
  
  public boolean updateCSVFile(List<ControlBean> controlBeans) {
    boolean isUpdated = false;
    return beanToCsv(controlBeans);
  }
  
  public boolean updateCSVFile(ControlBean controlBeans) {
    return beanToCsv(controlBeans);
  }
  
  private List<ControlBean> csvToBean() {
    logger.info("About to read control file for previous execution details ");
    List<ControlBean> beans = new ArrayList<>();
    try {
      Reader reader = new BufferedReader(new FileReader(prepareControlFileName()));
      CsvToBean<ControlBean> csvReader = (new CsvToBeanBuilder(reader))
        .withType(ControlBean.class).withSeparator(',')
        .withIgnoreLeadingWhiteSpace(true)
        .withIgnoreEmptyLine(true)
        .build();
      beans = csvReader.parse();
      logger.info("Successfully read the control file for previous execution details ");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      logger.error("Unable to read previous attempt file process details from control file ,due to error :" + 
          e.getMessage());
    } 
    return beans;
  }
  
  private boolean beanToCsv(ControlBean bean) {
    logger.info("About to write the file process details into Control file ");
    boolean isUpdated = false;
    try {
      FileWriter writer = new FileWriter(prepareControlFileName(), true);
      ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
      mappingStrategy.setType(ControlBean.class);
      mappingStrategy.setColumnMapping(header);
      StatefulBeanToCsvBuilder<ControlBean> builder = new StatefulBeanToCsvBuilder(writer);
      StatefulBeanToCsv beanWriter = builder.withMappingStrategy((MappingStrategy)mappingStrategy).build();
      beanWriter.write(bean);
      isUpdated = true;
      writer.close();
      logger.info("Successfuly update the control file with file process details !");
    } catch (IOException e) {
      logger.error(
          "Unable to update the control file about file process details ,due to error :" + e.getMessage());
    } catch (CsvDataTypeMismatchException e) {
      logger.error(
          "Unable to update the control file about file process details ,due to error :" + e.getMessage());
    } catch (CsvRequiredFieldEmptyException e) {
      logger.error(
          "Unable to update the control file about file process details ,due to error :" + e.getMessage());
    } 
    return isUpdated;
  }
  
  private boolean beanToCsv(List controlBeans) {
    logger.info("About to write the file process details into Control file ");
    boolean isUpdated = false;
    try {
      FileWriter writer = new FileWriter(prepareControlFileName(), true);
      ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
      mappingStrategy.setType(ControlBean.class);
      mappingStrategy.setColumnMapping(header);
      StatefulBeanToCsvBuilder<ControlBean> builder = new StatefulBeanToCsvBuilder(writer);
      StatefulBeanToCsv beanWriter = builder.withMappingStrategy((MappingStrategy)mappingStrategy).build();
      beanWriter.write(controlBeans);
      isUpdated = true;
      writer.close();
      logger.info("Successfuly update the control file with file process details !");
    } catch (IOException e) {
      logger.error(
          "Unable to update the control file about file process details ,due to error :" + e.getMessage());
    } catch (CsvDataTypeMismatchException e) {
      logger.error(
          "Unable to update the control file about file process details ,due to error :" + e.getMessage());
    } catch (CsvRequiredFieldEmptyException e) {
      logger.error(
          "Unable to update the control file about file process details ,due to error :" + e.getMessage());
    } 
    return isUpdated;
  }
	public boolean isCSVContainsEntry(String value) {
		logger.info("Checking whether the file: " + value + " ,processed in eariler attempts ");
		boolean isPresent = false;
		List<ControlBean> beans = csvToBean();
		for (ControlBean bean : beans) {
			if (bean.getFileName().equals(value)) {
				isPresent = true;
				logger.info(
						"File: " + value + " ,processed in previous execution with process Id: " + bean.getProcessId()
								+ " ,on " + bean.getProcessedTime() + " with status:" + bean.getProcessStatus());
			}

		}
		return isPresent;
	}
}

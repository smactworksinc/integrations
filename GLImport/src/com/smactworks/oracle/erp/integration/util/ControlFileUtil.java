package com.smactworks.oracle.erp.integration.util;

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
import java.util.Optional;

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
import com.smactworks.oracle.erp.integration.bean.ControlBean;

public class ControlFileUtil {
	private static final Logger logger = LogManager.getLogger(ControlFileUtil.class);

	private boolean isControlFileExist() {
		if (!(new File(prepareControlFileName())).exists())
			return false;
		return true;
	}

	private String prepareControlFileName() {
		return String.valueOf(ConfigReader.getInstance().getControlFilePath().trim()) + "/"
				+ (new SimpleDateFormat("MMM")).format(new Date()) + " "
				+ (new SimpleDateFormat("YYYY")).format(new Date()) + ".csv";
	}

	public boolean createControlFile() {
		// String controlFileName = prepareControlFileName();
		// boolean controlFileCreated = false;
		// List<String[]> list = (List)new ArrayList<>();
		// list.add(Constants.CONTROL_FILE_HEADER_MAP);
		// logger.info("Creating control file " + controlFileName);
		/*
		 * try { Exception exception2, exception1 = null; } catch (IOException e) {
		 * logger.error("Unable to creat control file due to error :" + e.getMessage());
		 * } return controlFileCreated;
		 * 
		 */
		String controlFileName = prepareControlFileName();
		logger.info("About to creat Control file ");
		boolean controlFileCreated = false;
		List<String[]> list = new ArrayList<>();
		list.add(Constants.CONTROL_FILE_HEADER_MAP);
		try (CSVWriter writer = new CSVWriter(new FileWriter(controlFileName))) {
			writer.writeAll(list);
			logger.info("Control file : " + controlFileName + " created successfully !");
		} catch (IOException e) {
			logger.error("Unable to creat control file due to error :" + e.getMessage());
		}
		return controlFileCreated;
	}

	public Optional<ControlBean> readFromControlFile(String fileName) {
		if (!isControlFileExist())
			createControlFile();

		Optional<ControlBean> bean = Optional.empty();
		logger.info("About to read control file for previous execution details ");
		List<ControlBean> beans = new ArrayList<>();
		try (Reader reader = new BufferedReader(new FileReader(prepareControlFileName()));) {
			CsvToBean<ControlBean> csvReader = (new CsvToBeanBuilder(reader)).withType(ControlBean.class)
					.withSeparator(',').withIgnoreLeadingWhiteSpace(true).withIgnoreEmptyLine(true).build();
			beans = csvReader.parse();
			for (ControlBean obj : beans) {
				if (obj.getFileName().isPresent() && obj.getFileName().get().equals(fileName)
						&& !obj.getProcessStatus().get().equalsIgnoreCase("ERROR")) {
					bean = Optional.of(obj);
					break;
				}
			}
			logger.info("Successfully read the control file for previous execution details ");
		} catch (IOException e) {
			logger.error("Unable to read previous attempt file process details from control file ,due to error :"
					+ e.getMessage());
		}
		/*
		 * try { Reader reader = new BufferedReader(new
		 * FileReader(prepareControlFileName())); CsvToBean<ControlBean> csvReader =
		 * (new CsvToBeanBuilder(reader)).withType(ControlBean.class)
		 * .withSeparator(',').withIgnoreLeadingWhiteSpace(true).withIgnoreEmptyLine(
		 * true).build(); beans = csvReader.parse(); for (ControlBean obj : beans) { if
		 * (obj.getFileName() != null && obj.getFileName().equals(fileName)) { bean =
		 * Optional.of(obj); break; } } logger.
		 * info("Successfully read the control file for previous execution details "); }
		 * catch (FileNotFoundException e) { e.printStackTrace(); logger.
		 * error("Unable to read previous attempt file process details from control file ,due to error :"
		 * + e.getMessage()); }
		 */
		return bean;
	}

	public void writeToControlFile(ControlBean bean) {
		try {
			if (!isControlFileExist())
				createControlFile();
			FileWriter writer = new FileWriter(prepareControlFileName(), true);
			ColumnPositionMappingStrategy mappingStrategy = new ColumnPositionMappingStrategy();
			mappingStrategy.setType(ControlBean.class);
			mappingStrategy.setColumnMapping(Constants.CONTROL_FILE_HEADER_MAP);
			StatefulBeanToCsvBuilder<ControlBean> builder = new StatefulBeanToCsvBuilder(writer);
			StatefulBeanToCsv beanWriter = builder.withMappingStrategy((MappingStrategy) mappingStrategy).build();
			beanWriter.write(bean);
			writer.close();
		} catch (IOException e) {
			logger.error("Unable to write to control file ,due to error :" + e.getMessage());
		} catch (CsvDataTypeMismatchException e) {
			logger.error("Unable to write to control file ,due to error :" + e.getMessage());
		} catch (CsvRequiredFieldEmptyException e) {
			logger.error("Unable to write to control file ,due to error :" + e.getMessage());
		}
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

	private List<ControlBean> csvToBean() {
		logger.info("About to read control file for previous execution details ");
		List<ControlBean> beans = new ArrayList<ControlBean>();
		try {
			Reader reader = new BufferedReader(new FileReader(prepareControlFileName()));
			CsvToBean<ControlBean> csvReader = new CsvToBeanBuilder(reader).withType(ControlBean.class)
					.withSeparator(',').withIgnoreLeadingWhiteSpace(true).withIgnoreEmptyLine(true).build();
			beans = csvReader.parse();

			logger.info("Successfully read the control file for previous execution details ");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.error("Unable to read previous attempt file process details from control file ,due to error :"
					+ e.getMessage());
		}
		return beans;
	}

	public void writeToControlFile(List<ControlBean> bean) {
	}
}

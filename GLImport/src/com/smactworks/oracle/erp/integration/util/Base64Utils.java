package com.smactworks.oracle.erp.integration.util;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Base64Utils {
	private static final Logger logger = LogManager.getLogger(Base64Utils.class);

	public static String encodeBase64(String filePath) {
		LoggerUtils.getInstance(ConfigReader.getInstance().getLoggerFilePath().trim());
		logger.info("About to Convert file " + filePath + " base64 encoding format");
		String encodedBase64 = null;
		byte bytes[] = null;
		try (FileInputStream fis = new FileInputStream(new File(filePath))) {
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				byte[] buffer = new byte[1024];
				int read = -1;
				while ((read = fis.read(buffer)) != -1) {
					baos.write(buffer, 0, read);
				}
				bytes = baos.toByteArray();
			} catch (IOException exp) {
				exp.printStackTrace();
			}
			encodedBase64 = new String(Base64.getEncoder().encodeToString(bytes));
		} catch (FileNotFoundException e) {
			logger.info("File :" + filePath + " Not Found");
			e.printStackTrace();
		} catch (IOException e) {
			logger.info("Unable to read the file :" + filePath);
			e.printStackTrace();
		}
		logger.info("File :" + filePath + " Successfully Encoded");
		return encodedBase64;
	}

	public static byte[] decodeFromBase64(String encodedString) {
		return Base64.getDecoder().decode(encodedString);
	}
}


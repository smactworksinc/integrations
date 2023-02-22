package com.smactworks.oracle.erp.integration.util;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {
	public static void moveFile(String sourcePath, String destinationPath) throws IOException {
			Files.move(Paths.get(sourcePath), Paths.get(destinationPath));
	}

	public static String getFileExtension(String fileName) {
		return Paths.get(fileName).getFileName().toString()
				.substring(Paths.get(fileName).getFileName().toString().lastIndexOf('.') + 1);
	}

	public static String getArchiveFileName(String filePath, Long processId, String status) {
		SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
		String strDate = formatter.format(new Date());
		String fileSufix = "_PID_" + processId + "_" + strDate + "-" + status;
		String fileName = Paths.get(filePath).getFileName().toString().replaceFirst("[.][^.]+$", "");
		String fileExtension = Paths.get(filePath).getFileName().toString()
				.substring(Paths.get(filePath).getFileName().toString().lastIndexOf('.') + 1);
		return fileName + fileSufix + "." + fileExtension;

	}

	public static void writeBodyContentToFile(String body) {
		try {
			BufferedWriter f_writer = new BufferedWriter(new FileWriter(
					String.valueOf(ConfigReader.getInstance().getControlFilePath()) + "emailHtmlBody.txt"));
			f_writer.write(body);
			f_writer.close();
		} catch (IOException e) {
			System.out.print(e.getMessage());
		}
	}

	public static String getDuplicateFilePath(String filePath) {
		String fileName = Paths.get(filePath, new String[0]).getFileName().toString().replaceFirst("[.][^.]+$", "");
		String fileExtension = Paths.get(filePath).getFileName().toString()
				.substring(Paths.get(filePath).getFileName().toString().lastIndexOf('.') + 1);
		return String.valueOf(fileName) + '.' + fileExtension + "_" + Constants.FILE_PROCESS_DUPLICATE;
	}

	public static String writeToFile(byte[] byteArray, Long parentProcessId, String logFileDownloadPath) {
		String path=new StringBuilder(logFileDownloadPath).append(parentProcessId).append(Constants.FILE_EXTENSION_CSV).toString();
		try {
			Files.write(new File(path).toPath(), byteArray);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return path;
	}

	public static List<Long> getAllESSChildProcessIds(String processLogzipFilePath) {
		List<Long> childEssProcessLst = new ArrayList<>();
		FileInputStream fis = null;
		ZipInputStream zipIs = null;
		ZipEntry zEntry = null;
		try {
			fis = new FileInputStream(processLogzipFilePath);
			zipIs = new ZipInputStream(new BufferedInputStream(fis));
			while ((zEntry = zipIs.getNextEntry()) != null) {
				Long processId = extractProcessIdFromString(zEntry.getName());
				childEssProcessLst.add(processId);
			}
			zipIs.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			try {
				Files.deleteIfExists(Paths.get(processLogzipFilePath, new String[0]));
			} catch (NoSuchFileException noSuchFileException) {

			} catch (DirectoryNotEmptyException directoryNotEmptyException) {

			} catch (IOException iOException) {
			}
		}
		return childEssProcessLst;
	}

	private static Long extractProcessIdFromString(String str) {
		Long processId = null;
		Pattern pattern = Pattern.compile("[^0-9]");
		processId = new Long(pattern.matcher(str).replaceAll(""));
		return processId;
	}

	public static Optional<String> getMD5Checksum(File filename) {
		Optional<String> checkSum=Optional.empty();
		try {
			Optional.of(createChecksum(filename));
		}catch(IOException | NoSuchAlgorithmException e) {
			
		}
		return checkSum;
	}

	public static String createChecksum(File filename) throws IOException, NoSuchAlgorithmException{
		byte[] data = Files.readAllBytes(Paths.get(filename.getAbsolutePath()));
		byte[] hash = MessageDigest.getInstance("MD5").digest(data);
		return new BigInteger(1, hash).toString(16);
	}
}

package com.smactworks.erp.integration.rest.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import org.apache.commons.codec.binary.Base64;

public class FileManagementUtil {
  public static void main(String[] args) {
    File f = new File("E:/Inbound/BankStatements/UK/uk_Investment_StmtImport_2020JM02hhmmss.zip.zip");
    System.out.println(Paths.get("E:/Inbound/BankStatements/UK/uk_Investment_StmtImport_2020JM02hhmmss.zip.zip", new String[0]).getParent());
  }
  
  private static String encodeFileToBase64Binary(String fileName) throws IOException {
    System.out.println("you are here");
    File file = new File(fileName);
    byte[] bytes = loadFile(file);
    byte[] encoded = Base64.encodeBase64(bytes);
    String encodedString = new String(encoded);
    System.out.println("you are here twice");
    System.out.println("---" + encodedString);
    return encodedString;
  }
  
  private static byte[] loadFile(File file) throws IOException {
    InputStream is = new FileInputStream(file);
    long length = file.length();
    System.out.println(length);
    byte[] bytes = new byte[(int)length];
    int offset = 0;
    int numRead = 0;
    while (offset < bytes.length && (
      numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
      offset += numRead; 
    if (offset < bytes.length)
      throw new IOException("Could not completely read file " + file.getName()); 
    is.close();
    System.out.println(bytes.length);
    return bytes;
  }
}

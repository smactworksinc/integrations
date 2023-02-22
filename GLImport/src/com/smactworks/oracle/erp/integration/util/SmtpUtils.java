package com.smactworks.oracle.erp.integration.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SmtpUtils {
  public static void sendEmailUsingSMTP(String subject, StringBuilder body) throws Exception {
    try {
      MimeMessage msg;
      Properties props = new Properties();
      final String smtpAuthUserName = ConfigReader.getInstance().getSmtpAuthUserName();
      final String smtpAuthPassword = ConfigReader.getInstance().getSmtpAuthPassword();
      props.put("mail.smtp.host", ConfigReader.getInstance().getSmtpServer());
      props.put("mail.smtp.port", ConfigReader.getInstance().getSmtpPort());
      if (smtpAuthUserName != null && smtpAuthPassword != null) {
        props.put("mail.smtp.auth", Boolean.valueOf(true));
        props.put("mail.smtp.starttls.enable", Boolean.valueOf(true));
        Authenticator authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication(smtpAuthUserName, smtpAuthPassword);
            }
          };
        msg = new MimeMessage(Session.getInstance(props, authenticator));
      } else {
        msg = new MimeMessage(Session.getInstance(props, null));
      } 
      //msg.getSession().setDebug(false);
      msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
      msg.addHeader("format", "flowed");
      msg.addHeader("Content-Transfer-Encoding", "8bit");
      msg.setFrom((Address)new InternetAddress(ConfigReader.getInstance().getEmailFrom(), "NoReply-JournalUpload"));
      msg.setReplyTo((Address[])InternetAddress.parse(ConfigReader.getInstance().getEmailFrom(), false));
      msg.setSubject(subject, "UTF-8");
      msg.setContent(body.toString(), "text/html");
      msg.setSentDate(new Date());
      String[] recipientList = ConfigReader.getInstance().getEmailTo().split(",");
      InternetAddress[] recipientAddress = new InternetAddress[recipientList.length];
      int counter = 0;
      byte b;
      int i;
      String[] arrayOfString1;
      for (i = (arrayOfString1 = recipientList).length, b = 0; b < i; ) {
        String recipient = arrayOfString1[b];
        recipientAddress[counter] = new InternetAddress(recipient.trim());
        counter++;
        b++;
      } 
      msg.setRecipients(Message.RecipientType.TO, (Address[])recipientAddress);
      Transport.send((Message)msg);
    } catch (Exception e) {
      throw e;
    } 
  }
  
  public static void sendEmail(String subject, StringBuilder body) throws Exception {
    sendEmailUsingSMTP(subject, body);
  }
  
  public static String getEmailSubject(boolean currentInstanceStatus) {
    if (currentInstanceStatus)
      return ConfigReader.getInstance().getEmailSubjectSuccess(); 
    return ConfigReader.getInstance().getEmailSubjectFailure();
  }
  
  public static StringBuilder prepareHealthCheckEmailBody(String message) {
	  return new StringBuilder("<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Unable to process the files&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\\t\\t\\t\\t\\t: \n <b>Reason:</b>"+message);
  }
  
  public static StringBuilder prepareEmailBody(Map<String, List<String>> fileProcessStatusMap, Map<String, Long> processMap) {
	  
    long totalFilesUploadSuccess = fileProcessStatusMap.get("Success").size();
    long totalFilesUploadFailure = fileProcessStatusMap.get("Error").size();
    long totalFilesDuplicate = fileProcessStatusMap.get("Duplicate").size();
    
    long totalNumberOfFiles = totalFilesUploadSuccess + totalFilesUploadFailure + totalFilesDuplicate;
    StringBuilder strBuilder = new StringBuilder("<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Number of Files Processed&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\t\t\t\t\t: <b>" + 
        
        totalNumberOfFiles + "</b> <br>\n" + 
        "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Number of Files Processed(Success)&nbsp;&nbsp;\t\t\t: <b>" + 
        totalFilesUploadSuccess + "</b><br>\n" + 
        "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Number of Files Processed(Error)&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\t\t\t: <b>" + 
        totalFilesUploadFailure + "</b><br>\n" + 
        "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Number of Files Processed(Duplicate)\t\t: <b>" + 
        totalFilesDuplicate + "</b></p>\n" + "<p>\n" + "<table style=\"width:100%;border:1px solid black\">\n" + 
        "<th style='border:1px solid black;'><b>File Name</b></th>\n" + 
        "<th style='border:1px solid black;'><b>Status</b></th>\n" + 
        "<th style='border:1px solid black;'><b>Process ID</b></th>");
    for (Map.Entry<String, List<String>> set : fileProcessStatusMap.entrySet()) {
      if (((String)set.getKey()).equals("Success")) {
        for (String fileName : set.getValue()) {
          strBuilder.append("<tr style='border:1px solid black;'>");
          strBuilder.append("<td style='border:1px solid black;'>" + fileName + "</td>");
          strBuilder
            .append("<td style='border:1px solid black;' bgcolor='green'>Success</td>");
          strBuilder
            .append("<td style='border:1px solid black;' bgcolor='green'>" + processMap.get(fileName) + "</td>");
          strBuilder.append("</tr>");
        } 
        continue;
      } 
      if (((String)set.getKey()).equals("Error")) {
        for (String fileName : set.getValue()) {
          strBuilder.append("<tr style='border:1px solid black;'>");
          strBuilder.append("<td style='border:1px solid black;'>" + fileName + "</td>");
          strBuilder
            .append("<td style='border:1px solid black;' bgcolor='red'>Error</td>");
          strBuilder
            .append("<td style='border:1px solid black;' bgcolor='red'>" + processMap.get(fileName) + "</td>");
          strBuilder.append("</tr>");
        } 
        continue;
      } 
      if (((String)set.getKey()).equals("Duplicate"))
        for (String fileName : set.getValue()) {
          strBuilder.append("<tr style='border:1px solid black;'>");
          strBuilder.append("<td style='border:1px solid black;'>" + fileName + "</td>");
          strBuilder.append(
              "<td style='border:1px solid black;' bgcolor='yellow'>Duplicate</td>");
          strBuilder
            .append("<td style='border:1px solid black;' bgcolor='yellow'>" + processMap.get(fileName) + "</td>");
          strBuilder.append("</tr>");
        }  
    } 
    strBuilder.append("</table></p>");
    return strBuilder;
  }
}

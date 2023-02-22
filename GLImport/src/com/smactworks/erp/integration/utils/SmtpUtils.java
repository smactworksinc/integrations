package com.smactworks.erp.integration.utils;

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
      msg.getSession().setDebug(true);
      msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
      msg.addHeader("format", "flowed");
      msg.addHeader("Content-Transfer-Encoding", "8bit");
      msg.setFrom((Address)new InternetAddress(ConfigReader.getInstance().getEmailFrom(), "NoReply-JD"));
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
  
  public static StringBuilder prepareEmailBody(Map<String, List<String>> fileProcessStatusMap) {
    long totalFilesUploadSuccess = ((List)fileProcessStatusMap.get("SUCCESS")).size();
    long totalFilesUploadFailure = ((List)fileProcessStatusMap.get("FAILURE")).size();
    long totalFilesDuplicate = ((List)fileProcessStatusMap.get("DUPLICATE")).size();
    long totalNumberOfFiles = totalFilesUploadSuccess + totalFilesUploadFailure + totalFilesDuplicate;
    StringBuilder strBuilder = new StringBuilder("<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Number of Files Processed&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\t\t\t\t\t: <b>" + 
        
        totalNumberOfFiles + "</b> <br>\n" + 
        "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Number of Files Processed(Success)&nbsp;&nbsp;\t\t\t: <b>" + 
        totalFilesUploadSuccess + "</b><br>\n" + 
        "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Number of Files Processed(Error)&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\t\t\t: <b>" + 
        totalFilesUploadFailure + "</b><br>\n" + 
        "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Number of Files Processed(Duplicate)\t\t: <b>" + 
        totalFilesDuplicate + "</b></p>\n" + "<p>\n" + "<table style=\"width:100%;border:1px solid black\">\n" + 
        "<th style='border:1px solid black;'>File Name</th>\n" + 
        "<th style='border:1px solid black;'>Upload status</th>");
    for (Map.Entry<String, List<String>> set : fileProcessStatusMap.entrySet()) {
      if (((String)set.getKey()).equals("SUCCESS")) {
        for (String fileName : set.getValue()) {
          strBuilder.append("<tr style='border:1px solid black;'>");
          strBuilder.append("<td style='border:1px solid black;'>" + fileName + "</td>");
          strBuilder
            .append("<td style='border:1px solid black;' bgcolor='green'>SUCCESS</td>");
          strBuilder.append("</tr>");
        } 
        continue;
      } 
      if (((String)set.getKey()).equals("FAILURE")) {
        for (String fileName : set.getValue()) {
          strBuilder.append("<tr style='border:1px solid black;'>");
          strBuilder.append("<td style='border:1px solid black;'>" + fileName + "</td>");
          strBuilder
            .append("<td style='border:1px solid black;' bgcolor='red'>FAILURE</td>");
          strBuilder.append("</tr>");
        } 
        continue;
      } 
      if (((String)set.getKey()).equals("DUPLICATE"))
        for (String fileName : set.getValue()) {
          strBuilder.append("<tr style='border:1px solid black;'>");
          strBuilder.append("<td style='border:1px solid black;'>" + fileName + "</td>");
          strBuilder.append(
              "<td style='border:1px solid black;' bgcolor='yellow'>DUPLICATE</td>");
          strBuilder.append("</tr>");
        }  
    } 
    strBuilder.append("</table></p>");
    return strBuilder;
  }
}

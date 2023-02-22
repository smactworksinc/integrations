package com.smactworks.erp.integration.smtp;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.smactworks.oracle.erp.integration.util.ConfigReader;
import com.smactworks.oracle.erp.integration.util.LoggerUtils;


public class EmailClient {
	private static final Logger logger = LogManager.getLogger(EmailClient.class);
	public static boolean sendEmail(Map<String,ArrayList> fileDetails) {
		LoggerUtils.getInstance(ConfigReader.getInstance().getLoggerFilePath().trim());
		logger.info("About to send email notification");
		boolean isEmailSend=false;
		String to = ConfigReader.getInstance().getEmailTo().trim();
		String from = ConfigReader.getInstance().getEmailFrom().trim();
		String host = ConfigReader.getInstance().getSmtpServer().trim();// or IP address
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", host);
		Session session = Session.getDefaultInstance(properties);
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setText(ConfigReader.getInstance().getEmailBody().trim());
			Transport.send(message);
			isEmailSend=true;
			logger.info("Email notification send successfully !");

		} catch (MessagingException mex) {
			logger.error("Unable to sendf Email notification "+mex.getMessage());
		}
		return isEmailSend;
	}
}

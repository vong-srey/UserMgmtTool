package tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import ldap.EmailConstants;
import ldap.LdapProperty;
//import ldap.LdapTool;

import org.apache.log4j.Logger;

public class EmailClient {
	private static Properties mailServerConfig = new Properties();
	
	
	
	// all below constants are used for reading the values from configuration file
	public static final String REJECTED_SUBJECT = "rejected.subject";
	public static final String REJECTED_BODY_FILE_PATH = "rejected.body.file.path";

	public static final String APPROVED_SUBJECT = "approved.subject";
	public static final String APPROVED_BODY_FILE_PATH = "approved.body.file.path";
	public static final String APPROVED_PASSWORD_FILE_PATH = "approved.password.file.path";

	public static final String REPLACEKEY_RECIPIENTNAME = "UsrMgmt_RECIPIENTNAME";
	public static final String REPLACEKEY_USERNAME = "UsrMgmt_USERNAME";
	public static final String REPLACEKEY_PASSWORD = "UsrMgmt_PASSWORD";
	
	
	/**
	 * send an SMS to the given mobile phone number, with the given smsBody
	 * @param mobile phone number that will receive SMS. e.g. +64213456789. This phone number
	 * must be a valid one. If it is an invalid, the method will try to send, but, it will go silent
	 * without returning back a feedback.
	 * @param recipientName the reciever name (it can be an empty string, but not null)
	 * @param smsSubject the subject of the sms (it can be an empty string, but not null)
	 * @param smsBody the body of the txt message
	 */
	public static void sendSMSto(String mobile, String recipientName, String smsSubject, String smsBody){
		init();
		Logger logger = Logger.getRootLogger(); // initiate as a default root logger
		
		logger.debug("Preparing to send an SMS to: " + recipientName + " who has: " + mobile + " with the content: " + smsBody);
		
		String mobileAsSMS4UmailAddress = mobile + LdapProperty.getProperty("txt.msg.server.domain");
		
		
		Session session = Session.getDefaultInstance(mailServerConfig, null);
		MimeMessage message = new MimeMessage(session);
		try {
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(mobileAsSMS4UmailAddress));
			message.setFrom(new InternetAddress(LdapProperty.getProperty("mail.from")));
			message.setSubject(smsSubject);
			message.setText(smsBody);
			Transport.send(message);

		} catch (AddressException e) {
			logger.error("Could not send out an email",e);
		} catch (MessagingException e) {
			logger.error("Could not send out an email", e);
		}
		logger.debug("finished sending email");
	}
	
	
	// comment out the password part
	// Recipient name
	// username
	// template of the email contents (prefer reading from file)
	// reading from config file:
		// subject
		//
	public static void sendEmailApproved(String mailTo, String recipientName, String username, String password) throws Exception{
		init();
		Logger logger = Logger.getRootLogger(); // initiate as a default root logger
		
		logger.debug("preparing approved email.");
		
		String mailSubject = LdapProperty.getProperty(APPROVED_SUBJECT);
		
		
		String mailBodyFilePath = LdapProperty.getProperty(APPROVED_BODY_FILE_PATH);
		String mailBody = "";
		// reading mail body
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(mailBodyFilePath));
			String s=null;
			while((s = br.readLine()) != null){
				mailBody += s;
			}
		} catch (IOException e) {
			logger.error("Couldn't read mail body content file " + mailBodyFilePath, e);
		} finally {
			try {
				if(br!=null) br.close();
			} catch(IOException ex){
				logger.error("Couldn't close file: " + mailBodyFilePath, ex);
			}
		}
		// replace keys in mail body
		mailBody = mailBody.replace(REPLACEKEY_RECIPIENTNAME, recipientName);
		mailBody = mailBody.replace(REPLACEKEY_USERNAME, username);
		mailBody = mailBody.replace(REPLACEKEY_PASSWORD, password);
		
		
		String passwordMailFilePath = LdapProperty.getProperty(APPROVED_PASSWORD_FILE_PATH);
		String passwordMail = "";
		// reading mail body
		br = null;
		try{
			br = new BufferedReader(new FileReader(passwordMailFilePath));
			String s=null;
			while((s = br.readLine()) != null){
				passwordMail += s;
			}
		} catch (IOException e) {
			logger.error("Couldn't read password mail content file " + passwordMailFilePath, e);
		} finally {
			try {
				if(br!=null) br.close();
			} catch(IOException ex){
				logger.error("Couldn't close file: " + passwordMailFilePath, ex);
			}
		}
		// replace keys in mail body
		passwordMail = passwordMail.replace(REPLACEKEY_RECIPIENTNAME, recipientName);
		passwordMail = passwordMail.replace(REPLACEKEY_USERNAME, username);
		passwordMail = passwordMail.replace(REPLACEKEY_PASSWORD, password);
		
		
		logger.debug("Start sending emails out");
		Session session = Session.getDefaultInstance(mailServerConfig, null);
		MimeMessage message = new MimeMessage(session);
		try {
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
			message.setFrom(new InternetAddress(LdapProperty.getProperty("mail.from")));
			// sending approved email
			message.setSubject(mailSubject);
			message.setContent(mailBody, "text/html");
			message.saveChanges();
			Transport.send(message);
			
			// sending email contains password
			message.setSubject(mailSubject + " - attachement");
			message.setContent(passwordMail, "text/html");
			message.saveChanges();
			Transport.send(message);
		} catch (AddressException e) {
			logger.error("Could not send out an email",e);
			throw e;
		} catch (MessagingException e) {
			logger.error("Could not send out an email", e);
			throw e;
		}
		logger.debug("finished sending approved email.");
	}
	
	
	
	public static void sendEmailRejected(String mailTo, String recipientName) throws Exception{
		init();
		Logger logger = Logger.getRootLogger(); // initiate as a default root logger
		logger.debug("preparing rejected email.");
		
		String mailSubject = LdapProperty.getProperty(REJECTED_SUBJECT);
		
		String mailBodyFilePath = LdapProperty.getProperty(REJECTED_BODY_FILE_PATH);
		String mailBody = "";
		// reading mail body
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(mailBodyFilePath));
			String s=null;
			while((s = br.readLine()) != null){
				mailBody += s;
			}
		} catch (IOException e) {
			logger.error("Couldn't read mail body content file " + mailBodyFilePath, e);
		} finally {
			try {
				if(br!=null) br.close();
			} catch(IOException ex){
				logger.error("Couldn't close file: " + mailBodyFilePath, ex);
			}
		}
		// replace keys in mail body
		mailBody = mailBody.replace(REPLACEKEY_RECIPIENTNAME, recipientName);
		
		Session session = Session.getDefaultInstance(mailServerConfig, null);
		MimeMessage message = new MimeMessage(session);
		
		try {
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
			message.setFrom(new InternetAddress(LdapProperty.getProperty("mail.from")));
			message.setSubject(mailSubject);
			message.setContent(mailBody, "text/html");
			message.saveChanges();
			Transport.send(message);
		} catch (AddressException e) {
			logger.error("Could not send out an email",e);
			throw e;
		} catch (MessagingException e) {
			logger.error("Could not send out an email", e);
			throw e;
		}
		logger.debug("finished sending rejected email.");
	}
	
	private static void init(){
		mailServerConfig.put("mail.smtp.host", LdapProperty.getProperty(EmailConstants.MAIL_HOST));
	}
}

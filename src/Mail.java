import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class Mail {

	public Mail() {
		// TODO Auto-generated constructor stub
	}
	void send(String email, String asunto, String cuerpo) throws UnsupportedEncodingException {
		// Sender's email ID needs to be mentioned
	    String from = "presens@uabc.imeev.com";

	    // Get system properties
	    Properties properties = System.getProperties();

	    // Setup mail server
	    properties.setProperty("mail.smtp.host", "mail.imeev.com");
	    properties.setProperty("mail.smtp.port","587");
	    properties.setProperty("mail.smtp.auth", "true");

	    // Get the default Session object.
	    
	    Session session = Session.getDefaultInstance(properties,new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("presens@uabc.imeev.com", "Aan*e4xy");
            }
         });

	    try{
	    	// Create a default MimeMessage object.
	        MimeMessage message = new MimeMessage(session);

	        // Set From: header field of the header.
	        message.setFrom(new InternetAddress(from,"Blue App"));

	        // Set To: header field of the header.
	        message.addRecipient(Message.RecipientType.TO,
	                                  new InternetAddress(email));

	        // Set Subject: header field
	        message.setSubject(asunto, "UTF-8");

	        // Now set the actual message
	        message.setContent(cuerpo,"text/html");

	        // Send message
	        Transport.send(message);
	        System.out.println("Sent message successfully....");
	    }catch (MessagingException mex) {
	    	mex.printStackTrace();
	    }
	}
	public static boolean isValid(String email) {
        String ePattern = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
	}
}

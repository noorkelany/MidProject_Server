package data;

import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;

public class EmailSender {

	private static final String senderEmail = "brazialaa@gmail.com";
	private static final String senderPassword = "zprf smod hyhh eypc";
	private static Session session;

	// Static initializer block
	static {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");

		session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(senderEmail, senderPassword);
			}
		});
	}

	public static boolean sendEmailDesigned(String recipientEmail, String htmlContent) {
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(senderEmail));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
			message.setSubject("Auto BPark");
			message.setContent(htmlContent, "text/html"); // Specify the content type as HTML

			Transport.send(message);
			System.out.println("Email sent successfully.");
			return true;
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean sendEmail(String recipientEmail, String content) {
		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(senderEmail));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
			message.setSubject("Auto BPark");
			message.setText(content);

			String htmlContent = String.format(
					"""
							<html>
							<body style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px;">
							    <div style="max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); padding: 30px;">
							        <h2 style="color: #004080; text-align: center; margin-bottom: 20px;">Your Parking Code</h2>
							        <p style="font-size: 20px; text-align: center;">
							            Your code is: <span style="color: red; font-weight: bold;">%s</span>
							        </p>
							        <div style="text-align: center; margin-top: 30px;">
							            <img src="https://www.keflatwork.com/wp-content/uploads/2019/01/parking-lot-with-trees.jpg" alt="Parking Lot" style="width: 100%%; max-width: 550px; border-radius: 6px;" />
							        </div>
							         <p style="font-size: 16px; text-align: center; margin-top: 40px;">
							         Best regards,<br/>
							         <strong>Auto BPark</strong>
							     </p>
							    </div>
							</body>
							</html>
							""",
					content);

			message.setContent(htmlContent, "text/html"); // Specify the content type as HTML

			Transport.send(message);
			System.out.println("Email sent successfully.");
			return true;
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void main(String[] args) {
		sendEmail("brazialaa@gmail.com", "hiii");
	}

}

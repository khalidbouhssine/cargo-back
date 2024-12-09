package com.car.cargo.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    public void sendEmail(String to, String subject, String body) {
        final String fromEmail = "khalidbouhssine3@gmail.com"; // Replace with your email
        final String password = "mousqggmdagdtkpk"; // Replace with your email password

        // Configure SMTP properties
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // Replace with your SMTP server
        props.put("mail.smtp.port", "587"); // Port for TLS
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // Enable TLS

        // Authenticate with the mail server
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            // Create and configure the email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            // Send the email
            Transport.send(message);
            System.out.println("Email sent successfully!");
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}

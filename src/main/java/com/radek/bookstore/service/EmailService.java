package com.radek.bookstore.service;

import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

import static com.radek.bookstore.utils.constants.EmailConstants.*;

@Service
public class EmailService {

    public void sendActivationAccountMessage(String firstName, String activationLink, String email) throws MessagingException {
        Message message = createEmail(firstName, activationLink, email);
        Transport.send(message, message.getAllRecipients());
    }

    public void sendAddedNewUserMessage(String firstName, String activationLink, String password, String email) throws MessagingException {
        Message message = addNewUserMessage(firstName, activationLink, password, email);
        Transport.send(message, message.getAllRecipients());
    }

    public void resetPasswordMessage(String firstName, String activationLink, String email) throws MessagingException {
        Message message = resetPasswordEmail(firstName, activationLink, email);
        Transport.send(message, message.getAllRecipients());
    }

    private Message createEmail(String firstName, String activationLink, String email) throws MessagingException {
        MimeMessage message = populateMessage(email, EMAIL_SUBJECT);
        message.setText("Witaj "+firstName+", " +
                "\n\nWłaśnie dokonałaś/-eś rejestracji w serwisie Bookstore Dev" +
                "\n\nJeśli trafiłaś/-eś tu przez przypadek - informuję, że to nie jest prawdziwa księgarnia online, ale aplikacja treningowa, którą napisałem aby rozwijać swoje umiejętności programistyczne." +
                "\n\nAby aktywować utworzone konto - kliknij na link aktywacyjny: "+activationLink+"" +
                "\n\nBookstore Dev - The Support Team");
        message.saveChanges();
        return message;
    }

    private Message resetPasswordEmail(String firstName, String activationLink, String email) throws MessagingException {
        MimeMessage message = populateMessage(email, RESET_PASSWORD_SUBJECT);
        message.setText("Witaj "+firstName+", " +
                "\n\nTwoje hasło zostało zmienione." +
                "\n\nAby re-aktywować konto - kliknij w link aktywacyjny: "+activationLink+"" +
                "\n\nBookstore Dev - The Support Team");
        message.saveChanges();
        return message;
    }

    private Message addNewUserMessage(String firstName, String activationLink, String password, String email) throws MessagingException {
        MimeMessage message = populateMessage(email, ADD_NEW_USER_SUBJECT);
        message.setText("Witaj "+firstName+", " +
                "\n\nWłaśnie założyliśmy Ci konto w serwisie Bookstore Dev" +
                "\n\nAby aktywować utworzone konto - kliknij na link aktywacyjny: "+activationLink+"" +
                "\n\nZaloguj się do serwisu przy użyciu hasła: "+password+"" +
                "\n\nHasło możesz zmienić poprzez opcję resetu hasła." +
                "\n\nBookstore Dev - The Support Team");
        message.saveChanges();
        return message;
    }

    private MimeMessage populateMessage(String email, String subject) throws MessagingException {
        MimeMessage message = new MimeMessage(getEmailSession());
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(subject);
        message.setSentDate(new Date());
        return message;
    }

    private Session getEmailSession() {
        Properties properties = System.getProperties();
        properties.put(SMTP_HOST, GMAIL_SMTP_SERVER);
        properties.put(SMTP_AUTH, true);
        properties.put(SMTP_PORT, DEFAULT_PORT);
        properties.put(SMTP_STARTTLS_ENABLE, true);
        properties.put(SMTP_STARTTLS_REQUIRED, true);
        properties.put(MAIL_SMTP_SSL_TRUST, GMAIL_SMTP_SERVER);
        return Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
    }
}

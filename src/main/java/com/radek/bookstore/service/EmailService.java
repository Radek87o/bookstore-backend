package com.radek.bookstore.service;

import com.radek.bookstore.model.Order;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.radek.bookstore.utils.constants.EmailConstants.*;

@Service
public class EmailService {

    private final SpringTemplateEngine templateEngine;

    public EmailService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

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

    public void orderSummaryMessage(String email, Order order, String firstName) throws MessagingException {
        MimeMessage message = populateMessage(email, ORDER_SUMMARY_MESSAGE);

        MimeMessageHelper helper = new MimeMessageHelper(message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());

        Map<String, Object> properties = new HashMap<>();
        properties.put("totalPrice", setPriceFormat(order.getTotalPrice()));
        properties.put("totalQuantity", order.getTotalQuantity());
        properties.put("items", order.getOrderItems());
        properties.put("firstName", firstName);

        String html = getHtmlContent("order-summary", properties);

        helper.setTo(email);
        helper.setFrom(new InternetAddress(FROM_EMAIL));
        helper.setSubject(ORDER_SUMMARY_MESSAGE);
        helper.setText(html, true);
        Transport.send(message, message.getAllRecipients());
    }

    private String getHtmlContent(String templateName, Map<String, Object> properties) {
        Context context = new Context();
        context.setVariables(properties);
        return templateEngine.process(templateName, context);
    }

    private String setPriceFormat(BigDecimal price) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(0);
        df.setGroupingUsed(false);
        return df.format(price);
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

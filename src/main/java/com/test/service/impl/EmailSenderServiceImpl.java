package com.test.service.impl;

import com.test.controller.LoginController;
import com.test.domain.dto.User;
import com.test.service.EmailSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailSenderServiceImpl implements EmailSenderService {
    private final static Logger LOG = LoggerFactory.getLogger(EmailSenderServiceImpl.class);
    private final JavaMailSender mailSender;

    @Value("${support.email}")
    private String supportEmail;

    @Value("${app.url}")
    private String appUrl;

    @SuppressWarnings("SpringJavaAutowiringInspection") // Provided by Spring Boot
    @Autowired
    public EmailSenderServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    //TODO: use template for emails
    @Override
    @Async
    public void sendConfirmationEmail(User user, String token) {
        String subject = "Registration Confirmation";
        String confirmationUrl = appUrl + LoginController.REGISTRATION_CONFIRM_FULL_URL + "?token=" + token;
        String messageText = "Welcome " + user.getName() + "!<br>" +
                "<br>" +
                "You can confirm your account through the link below:" +
                "<br><br>" +
                "<a href=\"" + confirmationUrl +"\">Confirm your account</a>";

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false);
            helper.setFrom(supportEmail);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText("<html><body>" + messageText + "</body></html>", true);
        } catch (MessagingException e) {
            LOG.warn("Registration confirmation email to {}({}) send error: {}", user.getEmail(), user.getId(), e.toString());
            throw new RuntimeException(e);
        }
        mailSender.send(message);

        LOG.info("Registration confirmation email to {}({}) sent.", user.getEmail(), user.getId());
    }

    @Override
    @Async
    public void sendConfirmationSuccess(User user, String password) {
        String subject = "Registration";
        String messageText = "Welcome " + user.getName() + "!<br>" +
                "<br>" +
                "You have registered with:" +
                "<br>" +
                "Login: " + user.getEmail() +
                "<br>" +
                "Password: " + password +
                "<br>";

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false);
            helper.setFrom(supportEmail);
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText("<html><body>" + messageText + "</body></html>", true);
        } catch (MessagingException e) {
            LOG.warn(String.format("Confirmation success email to %s(%d) send error", user.getEmail(), user.getId()), e);
            throw new RuntimeException(e);
        }
        mailSender.send(message);

        LOG.info("Confirmation success email to {}({}) sent.", user.getEmail(), user.getId());
    }
}

package com.test.service.impl;

import com.test.domain.dto.User;
import com.test.domain.dto.VerificationToken;
import com.test.service.EmailSenderService;
import com.test.service.RegistrationService;
import com.test.service.UserDetailServiceCustom;
import com.test.util.SecureIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;

@Component
public class RegistrationServiceImpl implements RegistrationService {
    private final static Logger LOG = LoggerFactory.getLogger(RegistrationServiceImpl.class);

    private final UserDetailServiceCustom userDetailService;
    private final EmailSenderService emailSenderService;
    private final MessageSource messages;

    @Autowired
    public RegistrationServiceImpl(UserDetailServiceCustom userDetailService,
                                   EmailSenderService emailSenderService, MessageSource messages) {
        this.userDetailService = userDetailService;
        this.emailSenderService = emailSenderService;
        this.messages = messages;
    }

    @Override
    public User registerUser(User user) {
        User registeredUser = userDetailService.create(user);
        LOG.info("User registered for email: {} ({})", registeredUser.getEmail(), registeredUser.getId());
        return registeredUser;
    }

    @Override
    public void sendConfirmation(User user) {
        String token = SecureIDGenerator.generateId();
        userDetailService.createVerificationToken(user, token);
        emailSenderService.sendConfirmationEmail(user, token);
    }

    @Override
    public void confirm(String token) {
        VerificationToken verificationToken = userDetailService.getVerificationToken(token);
        if (verificationToken == null) {
            LOG.info("Invalid confirmation token: {}", token);
            throw new RuntimeException(messages.getMessage("user.confirmation.error.token-invalid", null, Locale.US));
        }

        if (verificationToken.isVerified()) {
            LOG.info("Account already verified. Email: {}, token: {}", verificationToken.getEmail(), token);
            throw new RuntimeException(messages.getMessage("user.confirmation.warn.verified", null, Locale.US));
        }

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            LOG.info("Expired confirmation token. Email: {}, token: {}", verificationToken.getEmail(), token);
            throw new RuntimeException(messages.getMessage("user.confirmation.error.token-expired", null, Locale.US));
        }

        userDetailService.confirm(verificationToken);

        LOG.info("Registration confirmed for email: {}", verificationToken.getEmail());
    }

    @Override
    public void sendConfirmationSuccess(User user, String password) {
        emailSenderService.sendConfirmationSuccess(user, password);
    }

}


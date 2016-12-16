package com.test.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.mail.MessagingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
@Profile("local_smtp")
public class LocalSmtpServer {
    private final Logger LOG = LoggerFactory.getLogger(LocalSmtpServer.class);

    @Value("${spring.mail.port}")
    private int PORT = 2500;
    @Value("${spring.mail.host}")
    private String HOSTNAME = "localhost";
    private final Wiser wiser;

    @Autowired
    public LocalSmtpServer(JavaMailSenderImpl mailSender) throws MessagingException {
        wiser = new Wiser(){
            @Override
            public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException {
                byte[] bytes = StreamUtils.copyToByteArray(data);
                super.deliver(from, recipient, new ByteArrayInputStream(bytes));
                LOG.debug("Message from {} to {} delivered\n{}", from, recipient, new String(bytes));
            }
        };
        wiser.setHostname(HOSTNAME);
        wiser.setPort(PORT);
    }

    @PostConstruct
    private void init() {
        wiser.start();
    }

    public List<WiserMessage> getMessages() {
        return wiser.getMessages();
    }

    @PreDestroy
    private void destroy() {
        wiser.stop();
    }
}

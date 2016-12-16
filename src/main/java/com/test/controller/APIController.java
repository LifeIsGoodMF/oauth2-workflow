package com.test.controller;

import com.test.domain.dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class APIController {
    private final Logger LOG = LoggerFactory.getLogger(APIController.class);

    private final MessageSource messages;

    @Autowired
    public APIController(MessageSource messages) {
        this.messages = messages;
    }

    @RequestMapping(value = "/test", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public User greeting(@AuthenticationPrincipal User credential) {
        if (credential == null) throw new UsernameNotFoundException("");

        LOG.info("User {}({}) loged in.", credential.getId(), credential.getEmail());
        return credential;
    }


}

package com.test.controller;

import com.test.controller.validation.RegistrationCheckGroup;
import com.test.domain.dto.User;
import com.test.protocol.general.ErrorResponse;
import com.test.protocol.general.LoginResponse;
import com.test.protocol.general.Response;
import com.test.service.RegistrationService;
import com.test.service.UserDetailServiceCustom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

import static com.test.controller.LoginController.USERS_URL;

@RestController
@RequestMapping(USERS_URL)
public class LoginController {
    private final Logger LOG = LoggerFactory.getLogger(LoginController.class);

    public static final String USERS_URL = "users";
    public static final String REGISTRATION_CONFIRM_URL = "/confirm";
    public static final String REGISTRATION_CONFIRM_FULL_URL = USERS_URL + REGISTRATION_CONFIRM_URL;

    private final UserDetailServiceCustom userService;
    private final MessageSource messages;
    private final RegistrationService registrationService;

    @Autowired
    public LoginController(UserDetailServiceCustom userService, MessageSource messages, RegistrationService registrationService) {
        this.userService = userService;
        this.messages = messages;
        this.registrationService = registrationService;
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE )
    public Response registerUserAccount(@RequestBody @Validated(RegistrationCheckGroup.class) User user, HttpServletRequest req) {
        LOG.info("Registration request email: {}", user.getEmail());
        User registeredUser = registrationService.registerUser(user);
        try {
            registrationService.sendConfirmation(registeredUser);
        } catch (Exception e) {
            LOG.error("Can not send confirm registration link", e);
            return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    messages.getMessage("message.email.send.error", null, Locale.US), req.getRequestURI());
        }
        return new Response();
    }

    @RequestMapping(value = REGISTRATION_CONFIRM_URL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response confirmRegistration(@RequestParam("token") String token, HttpServletRequest req) {
        try {
            registrationService.confirm(token);
        } catch (Exception e) {
            LOG.error("Can not send confirm registration link", e);
            return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    e.getMessage(), req.getRequestURI());
        }

        return new Response();
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponse greeting(@AuthenticationPrincipal User credential) {
        if (credential == null) throw new UsernameNotFoundException("");

        LOG.info("User {}({}) loged in.", credential.getEmail(), credential.getId());
        return new LoginResponse(credential);
    }


    @RequestMapping(value = "/delete", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Response delete(@AuthenticationPrincipal User credential) {
        if (credential == null) throw new UsernameNotFoundException("");

        userService.deleteByIdOrEmail(null, credential.getEmail());
        LOG.info("User {}({}) deleted.", credential.getEmail(), credential.getId());


        return new Response();
    }
}

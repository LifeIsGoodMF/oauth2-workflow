package com.test.service;

import com.test.domain.dto.User;

public interface EmailSenderService {
    
    void sendConfirmationEmail(User user, String token);

    void sendConfirmationSuccess(User user, String password);
}

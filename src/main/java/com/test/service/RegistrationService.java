package com.test.service;

import com.test.domain.dto.User;

public interface RegistrationService {
    User registerUser(User user);
    void sendConfirmation(User user);
    void confirm(String token);
    void sendConfirmationSuccess(User user, String password);
}

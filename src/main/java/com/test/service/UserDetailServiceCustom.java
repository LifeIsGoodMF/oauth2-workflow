package com.test.service;

import com.test.domain.dto.User;
import com.test.domain.dto.VerificationToken;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserDetailServiceCustom extends UserDetailsService {
    User create(User userEntity);
    User deleteByIdOrEmail(Long id, String email);
    User findByIdOrEmail(Long id, String email);

    VerificationToken createVerificationToken(User user, String token);

    VerificationToken getVerificationToken(String token);

    void confirm(VerificationToken verificationToken);
}

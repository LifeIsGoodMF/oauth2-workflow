package com.test.domain.dto;

import com.test.domain.*;

public class DTOHelper {
    private DTOHelper(){}

    public static User detach(UserEntity userEntity) {
        User user = new User();
        user.setId(userEntity.getId());
        user.setName(userEntity.getName());
        user.setAdmin(userEntity.isAdmin());
        user.setEmail(userEntity.getEmail());
        user.setEnabled(userEntity.isEnabled());
        return user;
    }

    public static VerificationToken detach(VerificationTokenEntity verificationTokenEntity) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(verificationTokenEntity.getToken());
        verificationToken.setExpiryDate(verificationTokenEntity.getExpiryDate());
        verificationToken.setVerified(verificationTokenEntity.isVerified());
        verificationToken.setEmail(verificationTokenEntity.getUser().getEmail());
        return verificationToken;
    }
}

package com.test.domain.dto;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Size;
import java.time.Instant;

public class VerificationToken {
    @Size(max = 256)
    @NotBlank
    private String token;

    @Size(max = 50)
    @NotBlank
    private String email;

    private Instant expiryDate;
    private boolean verified;

    public VerificationToken() {
    }

    public VerificationToken(String token, String email, Instant expiryDate, boolean verified) {
        this.token = token;
        this.email = email;
        this.expiryDate = expiryDate;
        this.verified = verified;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}

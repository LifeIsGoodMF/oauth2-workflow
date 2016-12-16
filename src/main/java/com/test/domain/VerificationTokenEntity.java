package com.test.domain;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "verification_token")
public class VerificationTokenEntity {
    private static final int EXPIRATION_HOURS = 24;

    @Id
    @Size(max = 256)
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "verified")
    private boolean verified;

    public VerificationTokenEntity() {
    }
    public VerificationTokenEntity(String token, UserEntity user) {
        this.token = token;
        this.user = user;
        this.expiryDate = Instant.now().plus(EXPIRATION_HOURS, ChronoUnit.HOURS);
    }

    public String getToken() {
        return token;
    }

    public UserEntity getUser() {
        return user;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}

package com.test.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "early_subscription_user")
public class EarlySubscriptionUserEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private String email;

    @Column(name = "subscription_instant", nullable = false)
    private Instant subscriptionDateTime;

    EarlySubscriptionUserEntity() {
    }

    private EarlySubscriptionUserEntity(String email, Instant subscriptionDateTime) {
        this.email = email;
        this.subscriptionDateTime = subscriptionDateTime;
    }

    public static EarlySubscriptionUserEntity of(String email) {
        return new EarlySubscriptionUserEntity(email, Instant.now());
    }

    public String getEmail() {
        return email;
    }

    public Instant getSubscriptionDateTime() {
        return subscriptionDateTime;
    }
}

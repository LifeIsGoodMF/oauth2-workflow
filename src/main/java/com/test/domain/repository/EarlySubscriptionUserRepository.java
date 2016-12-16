package com.test.domain.repository;

import com.test.domain.EarlySubscriptionUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EarlySubscriptionUserRepository extends JpaRepository<EarlySubscriptionUserEntity, String> {

    default boolean exists(EarlySubscriptionUserEntity subscriptionUserEntity) {
        return exists(subscriptionUserEntity.getEmail());
    }
}

package com.test.domain.repository;

import com.test.domain.VerificationTokenEntity;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;

public interface VerificationTokenRepository extends CrudRepository<VerificationTokenEntity, String> {
    List<VerificationTokenEntity> deleteByExpiryDateLessThan(Instant dateTime);
}

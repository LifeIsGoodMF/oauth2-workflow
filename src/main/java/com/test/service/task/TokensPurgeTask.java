package com.test.service.task;

import com.test.domain.UserEntity;
import com.test.domain.VerificationTokenEntity;
import com.test.domain.repository.UserRepository;
import com.test.domain.repository.VerificationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TokensPurgeTask {
    private final static Logger LOG = LoggerFactory.getLogger(TokensPurgeTask.class);

    private final VerificationTokenRepository tokenRepository;

    private final UserRepository usersRepo;

    @Autowired
    public TokensPurgeTask(VerificationTokenRepository tokenRepository, UserRepository usersRepo) {
        this.tokenRepository = tokenRepository;
        this.usersRepo = usersRepo;
    }

    @Scheduled(cron = "${purge.cron.expression}")
    @Transactional
    public void purgeExpired() {
        LOG.info("{} start", TokensPurgeTask.class.getSimpleName());

        List<UserEntity> usersToDelete = tokenRepository.deleteByExpiryDateLessThan(Instant.now())
                .stream()
                .filter(vt -> !vt.isVerified())
                .map(VerificationTokenEntity::getUser)
                .collect(Collectors.toList());
        usersRepo.delete(usersToDelete);

        if (!usersToDelete.isEmpty()) {
            List<String> emails = usersToDelete.stream().map(UserEntity::getEmail).collect(Collectors.toList());
            LOG.info("Unverified users deleted: {}", emails);
        }

        LOG.info("{} end", TokensPurgeTask.class.getSimpleName());
    }
}

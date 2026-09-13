package com.starter.feature.auth.service;

import com.starter.feature.auth.config.EmailVerificationProperties;
import com.starter.feature.auth.entity.VerificationToken;
import com.starter.feature.auth.repository.VerificationTokenRepository;
import com.starter.feature.user.entity.User;
import com.starter.common.exception.BadRequestException;
import com.starter.common.util.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VerificationTokenService {

    private final VerificationTokenRepository tokenRepository;
    private final VerificationTokenGenerator tokenGenerator;
    private final EmailVerificationProperties properties;
    private final Clock clock;

    @Transactional
    public String issueToken(User user) {
        String tokenValue = this.tokenGenerator.generate();
        String tokenHash = HashUtil.sha256(tokenValue);

        Instant now = Instant.now(this.clock);
        Instant expiryDate = now.plus(this.properties.tokenExpirationHours());

        VerificationToken token = this.tokenRepository.findByUser(user)
                .orElseGet(() -> VerificationToken.builder()
                        .user(user)
                        .build());

        token.setTokenHash(tokenHash);
        token.setExpiryDate(expiryDate);
        token.setCreatedAt(now);

        this.tokenRepository.save(token);

        return tokenValue;
    }

    @Transactional
    public User consumeToken(String tokenValue) {
        VerificationToken token = this.tokenRepository
                .findByTokenHash(HashUtil.sha256(tokenValue))
                .orElseThrow(() -> new BadRequestException(
                        "Invalid verification link. It may have already been used or does not exist.")
                );

        Instant now = Instant.now(this.clock);
        if (token.getExpiryDate().isBefore(now)) {
            throw new BadRequestException(
                    "Verification link has expired. Please request a new one.");
        }

        User user = token.getUser();

        this.tokenRepository.delete(token);

        return user;
    }

    @Scheduled(cron = "${app.email-verification.cleanup-cron:0 0 3 * * ?}")
    @Transactional
    public void cleanupExpiredTokens() {
        this.tokenRepository.deleteAllExpiredBefore(Instant.now(this.clock));
    }
}

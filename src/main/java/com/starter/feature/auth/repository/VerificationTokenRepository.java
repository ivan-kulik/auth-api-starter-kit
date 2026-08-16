package com.starter.feature.auth.repository;

import com.starter.feature.auth.entity.VerificationToken;
import com.starter.feature.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface VerificationTokenRepository
        extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUser(User user);

    @Modifying
    @Query("""
        delete from VerificationToken vt
        where vt.expiryDate < :now
    """)
    int deleteAllExpiredBefore(@Param("now") Instant now);
}

package com.starter.feature.auth.repository;

import com.starter.feature.auth.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByTokenHashAndUserId(String tokenHash, Long userId);

    void deleteByUserId(Long userId);

    @Modifying
    @Query("""
    delete from RefreshToken rt
    where rt.revoked = true
       or rt.expiryDate < :now
""")
    void deleteExpiredOrRevoked(@Param("now") Instant now);
}

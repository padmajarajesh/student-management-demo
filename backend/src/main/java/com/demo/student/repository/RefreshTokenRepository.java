package com.demo.student.repository;

import com.demo.student.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.tokenHash = :tokenHash")
    int deleteByTokenHash(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.user.id = :userId AND t.expiresAt < :now")
    int deleteExpiredForUser(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
}

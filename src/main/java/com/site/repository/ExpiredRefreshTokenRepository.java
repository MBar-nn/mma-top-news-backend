package com.site.repository;

import com.site.model.ExpiredRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface ExpiredRefreshTokenRepository extends JpaRepository<ExpiredRefreshToken, Long> {

    Optional<ExpiredRefreshToken> findByRefreshToken(String refreshToken);

    boolean existsByRefreshToken(String refreshToken);

    @Modifying
    @Query("DELETE FROM ExpiredRefreshToken e WHERE e.expiredAt < :cutoffDate")
    void deleteExpiredTokensBefore(OffsetDateTime cutoffDate);
}
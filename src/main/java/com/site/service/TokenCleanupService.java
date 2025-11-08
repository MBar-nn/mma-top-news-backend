package com.site.service;

import com.site.repository.ExpiredRefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@Slf4j
public class TokenCleanupService {

    private final ExpiredRefreshTokenRepository expiredTokenRepository;

    public TokenCleanupService(ExpiredRefreshTokenRepository expiredTokenRepository) {
        this.expiredTokenRepository = expiredTokenRepository;
    }

/**

 Usuwa wygasłe tokeny starsze niż 30 dni.
 Wykonuje się codziennie o 3:00 w nocy./
 @Scheduled(cron = "0 0 3 * ?")@Transactional
 public void cleanupExpiredTokens() {
 OffsetDateTime cutoffDate = OffsetDateTime.now(ZoneOffset.UTC).minusDays(30);
 try {
 expiredTokenRepository.deleteExpiredTokensBefore(cutoffDate);
 log.info("Czyszczenie wygasłych tokenów zakończone pomyślnie");} catch (Exception e) {
 log.error("Błąd podczas czyszczenia wygasłych tokenów", e);}}
 **/
 }
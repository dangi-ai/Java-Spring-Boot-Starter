package com.technoboost.config;

import com.technoboost.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {

    private final RefreshTokenService refreshTokenService;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredRefreshTokens() {
        log.info("Running scheduled cleanup of expired refresh tokens");
        refreshTokenService.deleteExpiredTokens();
    }
}

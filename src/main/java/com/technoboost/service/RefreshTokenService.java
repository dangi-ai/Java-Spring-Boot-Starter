package com.technoboost.service;

import com.technoboost.entity.RefreshToken;
import com.technoboost.entity.User;
import com.technoboost.exception.TokenRefreshException;
import com.technoboost.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build();

        refreshToken = refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token created for user id: {}", user.getId());
        return refreshToken;
    }

    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Invalid refresh token presented");
                    return new TokenRefreshException("Invalid refresh token");
                });

        if (refreshToken.isExpired()) {
            refreshToken.setDeleted(true);
            refreshTokenRepository.save(refreshToken);
            log.warn("Expired refresh token used for user id: {}", refreshToken.getUser().getId());
            throw new TokenRefreshException("Refresh token has expired. Please sign in again");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.debug("All refresh tokens revoked for user id: {}", userId);
    }

    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
        log.info("Expired refresh tokens cleanup completed");
    }
}

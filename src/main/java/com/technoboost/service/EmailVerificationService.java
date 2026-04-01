package com.technoboost.service;

import com.technoboost.entity.EmailVerificationToken;
import com.technoboost.entity.User;
import com.technoboost.exception.BadRequestException;
import com.technoboost.exception.ResourceNotFoundException;
import com.technoboost.repository.EmailVerificationTokenRepository;
import com.technoboost.repository.UserRepository;
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
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Value("${app.email-verification.token-expiration-hours:24}")
    private int tokenExpirationHours;

    @Transactional
    public String createVerificationToken(User user) {
        // Invalidate any existing unused token
        tokenRepository.findByUserIdAndUsedFalse(user.getId())
                .ifPresent(existing -> existing.setDeleted(true));

        EmailVerificationToken token = EmailVerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plusSeconds(tokenExpirationHours * 3600L))
                .build();

        token = tokenRepository.save(token);
        log.info("Email verification token created for user id: {}", user.getId());

        // TODO: Replace with actual email sending (SMTP, SES, etc.)
        log.info("Verification link: /api/v1/auth/verify-email?token={}", token.getToken());

        return token.getToken();
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new BadRequestException("Verification token has already been used");
        }

        if (verificationToken.isExpired()) {
            throw new BadRequestException("Verification token has expired. Please request a new one");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        log.info("Email verified for user id: {}, email: {}", user.getId(), user.getEmail());
    }

    @Transactional
    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        createVerificationToken(user);
        log.info("Verification email resent for: {}", email);
    }
}

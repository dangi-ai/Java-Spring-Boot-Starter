package com.technoboost.service;

import com.technoboost.dto.request.LoginRequest;
import com.technoboost.dto.request.RefreshTokenRequest;
import com.technoboost.dto.request.RegisterRequest;
import com.technoboost.dto.response.AuthResponse;
import com.technoboost.dto.response.UserResponse;
import com.technoboost.entity.RefreshToken;
import com.technoboost.entity.User;
import com.technoboost.enums.Role;
import com.technoboost.exception.BadRequestException;
import com.technoboost.repository.UserRepository;
import com.technoboost.security.JwtTokenProvider;
import com.technoboost.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase(Locale.ROOT);
        log.debug("Processing registration for email: {}", email);

        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed - email already exists: {}", email);
            throw new BadRequestException("Email is already registered");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: id={}, email={}", user.getId(), user.getEmail());
        return UserResponse.from(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase(Locale.ROOT);
        log.debug("Authenticating user: {}", email);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(userPrincipal.getEmail())
                .orElseThrow();

        refreshTokenService.revokeAllUserTokens(user.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("User authenticated successfully: id={}, email={}", user.getId(), user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.debug("Processing token refresh request");

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        User user = refreshToken.getUser();

        String roles = user.getRole().name();
        String accessToken = jwtTokenProvider.generateAccessTokenFromEmail(
                user.getEmail(), user.getId(), roles);

        refreshToken.setDeleted(true);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        log.debug("Token refreshed for user id: {}", user.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .build();
    }

    @Transactional
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            refreshTokenService.revokeAllUserTokens(userPrincipal.getId());
            log.info("User logged out: id={}, email={}", userPrincipal.getId(), userPrincipal.getEmail());
        }
        SecurityContextHolder.clearContext();
    }
}

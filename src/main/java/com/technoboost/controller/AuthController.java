package com.technoboost.controller;

import com.technoboost.dto.request.LoginRequest;
import com.technoboost.dto.request.RefreshTokenRequest;
import com.technoboost.dto.request.RegisterRequest;
import com.technoboost.dto.response.ApiResponse;
import com.technoboost.dto.response.AuthResponse;
import com.technoboost.dto.response.UserResponse;
import com.technoboost.service.AuthService;
import com.technoboost.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email: {}", request.getEmail());
        UserResponse user = authService.register(request);
        log.info("User registered successfully with id: {}", user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        AuthResponse authResponse = authService.login(request);
        log.info("Login successful for email: {}", request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Token refresh request received");
        AuthResponse authResponse = authService.refreshToken(request);
        log.debug("Token refreshed successfully");
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        log.info("Logout request received");
        authService.logout();
        log.info("User logged out successfully");
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        log.info("Email verification request received");
        emailVerificationService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@RequestParam String email) {
        log.info("Resend verification request for email: {}", email);
        emailVerificationService.resendVerification(email);
        return ResponseEntity.ok(ApiResponse.success("Verification email sent"));
    }
}

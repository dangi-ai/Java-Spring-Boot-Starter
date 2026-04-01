package com.technoboost.controller;

import com.technoboost.dto.response.ApiResponse;
import com.technoboost.dto.response.UserResponse;
import com.technoboost.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        log.debug("Fetching current user profile");
        UserResponse user = userService.getCurrentUser();
        log.debug("Current user profile retrieved for id: {}", user.getId());
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }
}

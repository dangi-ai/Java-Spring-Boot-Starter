package com.technoboost.service;

import com.technoboost.dto.response.UserResponse;
import com.technoboost.entity.User;
import com.technoboost.exception.ResourceNotFoundException;
import com.technoboost.repository.UserRepository;
import com.technoboost.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        log.debug("Fetching user profile for email: {}", principal.getEmail());

        User user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> {
                    log.error("Authenticated user not found in database: {}", principal.getEmail());
                    return new ResourceNotFoundException("User", "email", principal.getEmail());
                });

        return UserResponse.from(user);
    }
}

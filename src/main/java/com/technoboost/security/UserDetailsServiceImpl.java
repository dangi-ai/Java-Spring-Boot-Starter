package com.technoboost.security;

import com.technoboost.entity.User;
import com.technoboost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = email.toLowerCase(Locale.ROOT);
        log.debug("Loading user details for email: {}", normalizedEmail);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Authentication failed - user not found: {}", normalizedEmail);
                    return new UsernameNotFoundException("User not found with email: " + normalizedEmail);
                });

        log.debug("User details loaded: id={}, active={}", user.getId(), user.isActive());
        return UserPrincipal.create(user);
    }
}

package com.technoboost.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.auth.max-requests:20}")
    private int maxRequests;

    @Value("${app.rate-limit.auth.window-seconds:60}")
    private int windowSeconds;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = resolveClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(ip, k -> new Bucket(maxRequests, windowSeconds));

        if (!bucket.tryConsume()) {
            log.warn("Rate limit exceeded for IP: {}, path: {}", ip, request.getRequestURI());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Too many requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/auth/");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    private static class Bucket {
        private final int maxRequests;
        private final int windowMillis;
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart;

        Bucket(int maxRequests, int windowSeconds) {
            this.maxRequests = maxRequests;
            this.windowMillis = windowSeconds * 1000;
            this.windowStart = System.currentTimeMillis();
        }

        boolean tryConsume() {
            long now = System.currentTimeMillis();
            if (now - windowStart > windowMillis) {
                synchronized (this) {
                    if (now - windowStart > windowMillis) {
                        count.set(0);
                        windowStart = now;
                    }
                }
            }
            return count.incrementAndGet() <= maxRequests;
        }
    }
}

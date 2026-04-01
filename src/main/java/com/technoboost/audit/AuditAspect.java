package com.technoboost.audit;

import com.technoboost.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final ApplicationEventPublisher eventPublisher;

    @Around("within(com.technoboost.controller..*)")
    public Object auditControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            publishAuditEvent(joinPoint, startTime);
        }
    }

    private void publishAuditEvent(ProceedingJoinPoint joinPoint, long startTime) {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return;
            }

            HttpServletRequest request = attrs.getRequest();
            HttpServletResponse response = attrs.getResponse();

            Long userId = null;
            String email = null;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
                userId = principal.getId();
                email = principal.getEmail();
            }

            String action = resolveAction(request.getMethod(), request.getRequestURI());

            AuditEvent event = AuditEvent.builder()
                    .userId(userId)
                    .email(email)
                    .action(action)
                    .httpMethod(request.getMethod())
                    .requestUri(request.getRequestURI())
                    .statusCode(response != null ? response.getStatus() : 0)
                    .ipAddress(resolveClientIp(request))
                    .userAgent(truncate(request.getHeader("User-Agent"), 500))
                    .durationMs(System.currentTimeMillis() - startTime)
                    .timestamp(LocalDateTime.now())
                    .build();

            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.debug("Failed to publish audit event: {}", e.getMessage());
        }
    }

    private String resolveAction(String method, String uri) {
        if (uri.contains("/auth/register")) return "USER_REGISTER";
        if (uri.contains("/auth/login")) return "USER_LOGIN";
        if (uri.contains("/auth/logout")) return "USER_LOGOUT";
        if (uri.contains("/auth/refresh-token")) return "TOKEN_REFRESH";
        if (uri.contains("/users/me")) return "VIEW_PROFILE";
        return method + " " + uri;
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

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}

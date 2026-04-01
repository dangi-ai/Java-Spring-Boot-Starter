package com.technoboost.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;

    @Async("auditExecutor")
    @EventListener
    public void handleAuditEvent(AuditEvent event) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(event.getUserId())
                    .email(event.getEmail())
                    .action(event.getAction())
                    .httpMethod(event.getHttpMethod())
                    .requestUri(event.getRequestUri())
                    .statusCode(event.getStatusCode())
                    .ipAddress(event.getIpAddress())
                    .userAgent(event.getUserAgent())
                    .durationMs(event.getDurationMs())
                    .timestamp(event.getTimestamp())
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to persist audit log: action={}, uri={}, error={}",
                    event.getAction(), event.getRequestUri(), e.getMessage());
        }
    }
}

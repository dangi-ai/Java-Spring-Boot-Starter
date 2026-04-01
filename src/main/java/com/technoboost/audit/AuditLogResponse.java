package com.technoboost.audit;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuditLogResponse {

    private Long id;
    private Long userId;
    private String email;
    private String action;
    private String httpMethod;
    private String requestUri;
    private int statusCode;
    private String ipAddress;
    private long durationMs;
    private LocalDateTime timestamp;

    public static AuditLogResponse from(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .email(auditLog.getEmail())
                .action(auditLog.getAction())
                .httpMethod(auditLog.getHttpMethod())
                .requestUri(auditLog.getRequestUri())
                .statusCode(auditLog.getStatusCode())
                .ipAddress(auditLog.getIpAddress())
                .durationMs(auditLog.getDurationMs())
                .timestamp(auditLog.getTimestamp())
                .build();
    }
}

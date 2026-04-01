package com.technoboost.audit;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuditEvent {

    private final Long userId;
    private final String email;
    private final String action;
    private final String httpMethod;
    private final String requestUri;
    private final int statusCode;
    private final String ipAddress;
    private final String userAgent;
    private final long durationMs;
    private final LocalDateTime timestamp;
}

package com.technoboost.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getUserActivity(Long userId, LocalDateTime from,
                                                   LocalDateTime to, Pageable pageable) {
        log.debug("Fetching audit logs for user id: {}, range: {} to {}", userId, from, to);
        return auditLogRepository
                .findByUserIdAndTimestampBetweenOrderByTimestampDesc(userId, from, to, pageable)
                .map(AuditLogResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getUserActivityByAction(Long userId, String action,
                                                           LocalDateTime from, LocalDateTime to,
                                                           Pageable pageable) {
        log.debug("Fetching audit logs for user id: {}, action: {}, range: {} to {}", userId, action, from, to);
        return auditLogRepository
                .findByUserIdAndActionAndTimestampBetweenOrderByTimestampDesc(userId, action, from, to, pageable)
                .map(AuditLogResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllActivity(LocalDateTime from, LocalDateTime to,
                                                  Pageable pageable) {
        log.debug("Fetching all audit logs, range: {} to {}", from, to);
        return auditLogRepository
                .findByTimestampBetweenOrderByTimestampDesc(from, to, pageable)
                .map(AuditLogResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getUserActivityToday(Long userId, Pageable pageable) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        return getUserActivity(userId, startOfDay, now, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getUserActivityLastWeek(Long userId, Pageable pageable) {
        LocalDateTime weekAgo = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        return getUserActivity(userId, weekAgo, now, pageable);
    }
}

package com.technoboost.audit;

import com.technoboost.dto.response.ApiResponse;
import com.technoboost.dto.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAllActivity(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<AuditLogResponse> logs = PagedResponse.from(
                auditLogService.getAllActivity(from, to, pageable));
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved", logs));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getUserActivity(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String action,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<AuditLogResponse> logs;
        if (action != null && !action.isBlank()) {
            logs = PagedResponse.from(
                    auditLogService.getUserActivityByAction(userId, action, from, to, pageable));
        } else {
            logs = PagedResponse.from(
                    auditLogService.getUserActivity(userId, from, to, pageable));
        }
        return ResponseEntity.ok(ApiResponse.success("User audit logs retrieved", logs));
    }

    @GetMapping("/users/{userId}/today")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getUserActivityToday(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<AuditLogResponse> logs = PagedResponse.from(
                auditLogService.getUserActivityToday(userId, pageable));
        return ResponseEntity.ok(ApiResponse.success("Today's audit logs retrieved", logs));
    }

    @GetMapping("/users/{userId}/last-week")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getUserActivityLastWeek(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<AuditLogResponse> logs = PagedResponse.from(
                auditLogService.getUserActivityLastWeek(userId, pageable));
        return ResponseEntity.ok(ApiResponse.success("Last week's audit logs retrieved", logs));
    }
}

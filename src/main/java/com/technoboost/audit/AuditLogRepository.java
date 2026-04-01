package com.technoboost.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            Long userId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime from, LocalDateTime to, Pageable pageable);

    Page<AuditLog> findByUserIdAndActionAndTimestampBetweenOrderByTimestampDesc(
            Long userId, String action, LocalDateTime from, LocalDateTime to, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.email = :email " +
            "AND a.timestamp BETWEEN :from AND :to ORDER BY a.timestamp DESC")
    Page<AuditLog> findByEmailAndDateRange(String email, LocalDateTime from, LocalDateTime to, Pageable pageable);
}

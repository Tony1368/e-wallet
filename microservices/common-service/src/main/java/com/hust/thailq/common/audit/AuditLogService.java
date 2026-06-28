package com.hust.thailq.common.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(AuditLog auditLog) {
        try {
            if (auditLog.getTimestamp() == null) {
                auditLog.setTimestamp(Instant.now());
            }
            if (auditLog.getLevel() == null) {
                auditLog.setLevel(auditLog.getStatusCode() != null && auditLog.getStatusCode() >= 400 ? "ERROR" : "INFO");
            }
            // Truncate fields to avoid DB overflow
            auditLog.setRequestBody(truncate(auditLog.getRequestBody(), 1000));
            auditLog.setResponseBody(truncate(auditLog.getResponseBody(), 1000));
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.warn("Failed to save audit log: {}", e.getMessage());
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength - 3) + "..." : value;
    }
}

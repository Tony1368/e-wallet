package com.hust.thailq.common.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_request_id", columnList = "requestId"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_service", columnList = "serviceName"),
        @Index(name = "idx_audit_user", columnList = "username")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Correlation ID xuyên suốt toàn bộ luồng request */
    @Column(nullable = false, length = 36)
    private String requestId;

    /** Tên service phát sinh log */
    @Column(nullable = false, length = 50)
    private String serviceName;

    /** HTTP method: GET, POST, PUT, DELETE */
    @Column(length = 10)
    private String httpMethod;

    /** URI path: /api/v1/wallets/1/debit */
    @Column(length = 500)
    private String path;

    /** HTTP status code: 200, 201, 400, 500 */
    private Integer statusCode;

    /** Thời gian xử lý (ms) */
    private Long durationMs;

    /** Username từ JWT (X-Auth-User header) */
    @Column(length = 100)
    private String username;

    /** IP client gốc */
    @Column(length = 45)
    private String clientIp;

    /** Service gọi đến (caller) - nếu là inter-service call */
    @Column(length = 50)
    private String callerService;

    /** Request body summary (truncated, không log sensitive data) */
    @Column(length = 1000)
    private String requestBody;

    /** Response body summary hoặc error message */
    @Column(length = 1000)
    private String responseBody;

    /** Log level: INFO, WARN, ERROR */
    @Column(nullable = false, length = 10)
    private String level;

    /** Error class nếu có exception */
    @Column(length = 200)
    private String errorClass;

    /** Thời điểm ghi log */
    @Column(nullable = false)
    private Instant timestamp;
}

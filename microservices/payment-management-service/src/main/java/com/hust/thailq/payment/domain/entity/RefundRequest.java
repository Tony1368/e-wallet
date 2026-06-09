package com.hust.thailq.payment.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "refund_request")
public class RefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private Long walletId;

    @Column(nullable = false)
    private BigDecimal amount;

    private String reason;

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED

    private String requestedBy;

    private String approvedBy;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (status == null) status = "PENDING";
    }
}

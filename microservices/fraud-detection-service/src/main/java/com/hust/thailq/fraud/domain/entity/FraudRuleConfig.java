package com.hust.thailq.fraud.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "fraud_rule_config")
public class FraudRuleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ruleName;

    // === Gioi han so tien ===
    @Column(nullable = false)
    private BigDecimal maxTransactionAmount;

    @Column(nullable = false)
    private Integer maxDailyTransactions;

    @Column(nullable = false)
    private BigDecimal maxDailyAmount;

    // === Kiem tra vi tri dia ly (Geo-velocity) ===
    @Column(nullable = false)
    private Integer geoVelocityMinutes; // Thoi gian toi thieu giua 2 vi tri khac nhau (phut)

    @Column(nullable = false)
    private Boolean geoVelocityEnabled;

    // === Tan suat giao dich (Velocity) ===
    @Column(nullable = false)
    private Integer maxTransactionsPerMinute; // So giao dich toi da trong 1 phut

    @Column(nullable = false)
    private Integer velocityWindowSeconds; // Khung thoi gian kiem tra (giay)

    // === So tien bat thuong (Anomaly) ===
    @Column(nullable = false)
    private BigDecimal anomalyAmountMultiplier; // He so nhan voi trung binh de xac dinh bat thuong

    @Column(nullable = false)
    private Boolean anomalyEnabled;

    // === Trang thai chung ===
    @Column(nullable = false)
    private Boolean enabled;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}

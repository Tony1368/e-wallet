package com.hust.thailq.domain.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "user_activity")
@EqualsAndHashCode(of = {"activityId"})
public class UserActivity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_activity_seq_gen"
    )
    @SequenceGenerator(
            name = "user_activity_seq_gen",
            sequenceName = "user_activity_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(nullable = false, unique = true)
    private String activityId;

    @Column(nullable = false)
    private Instant activityTime;

    @Column(nullable = false, length = 50)
    private String activityType; // LOGIN, TRANSFER, WITHDRAW, ADD_FUNDS

    @Column(length = 500)
    private String description;

    @Column
    private BigDecimal amount;

    @Column(length = 34)
    private String fromWalletIban;

    @Column(length = 34)
    private String toWalletIban;

    @Column(length = 45) // IPv6 can be up to 45 characters
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 100)
    private String deviceType;

    @Column(length = 100)
    private String browser;

    @Column(length = 100)
    private String operatingSystem;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String region;

    @Column(length = 20)
    private String latitude;

    @Column(length = 20)
    private String longitude;

    @Column(length = 50)
    private String timezone;

    @Column(nullable = false)
    private Boolean isSuccessful = true;

    @Column(length = 500)
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", referencedColumnName = "id")
    private UserSession session;

    @PrePersist
    protected void onCreate() {
        if (activityTime == null) {
            activityTime = Instant.now();
        }
        if (activityId == null) {
            activityId = java.util.UUID.randomUUID().toString();
        }
    }
} 
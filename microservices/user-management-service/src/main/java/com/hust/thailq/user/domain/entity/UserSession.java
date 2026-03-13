package com.hust.thailq.user.domain.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "user_session")
@EqualsAndHashCode(of = {"sessionId"})
public class UserSession {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_session_seq_gen"
    )
    @SequenceGenerator(
            name = "user_session_seq_gen",
            sequenceName = "user_session_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(nullable = false, unique = true)
    private String sessionId;

    @Column(nullable = false)
    private Instant loginTime;

    @Column
    private Instant logoutTime;

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
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;
} 
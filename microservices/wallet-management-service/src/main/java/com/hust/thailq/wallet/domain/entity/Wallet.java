package com.hust.thailq.wallet.domain.entity;

import com.hust.thailq.wallet.domain.enums.WalletStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String iban;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant createdAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletStatus status;

    private String bankInfo;

    @Column(name = "branch_id")
    private Long branchId;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}

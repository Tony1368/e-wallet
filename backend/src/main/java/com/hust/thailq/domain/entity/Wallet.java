package com.hust.thailq.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hust.thailq.domain.enums.WalletStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "fromWallet", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Transaction> sentTransactions = new HashSet<>();

    @OneToMany(mappedBy = "toWallet", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Transaction> receivedTransactions = new HashSet<>();

    @Column(nullable = false)
    private Instant createdAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletStatus status;

    private String bankInfo;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public void addSentTransaction(Transaction transaction) {
        sentTransactions.add(transaction);
        transaction.setFromWallet(this);
    }

    public void removeSentTransaction(Transaction transaction) {
        sentTransactions.remove(transaction);
        transaction.setFromWallet(null);
    }

    public void addReceivedTransaction(Transaction transaction) {
        receivedTransactions.add(transaction);
        transaction.setToWallet(this);
    }

    public void removeReceivedTransaction(Transaction transaction) {
        receivedTransactions.remove(transaction);
        transaction.setToWallet(null);
    }
}



















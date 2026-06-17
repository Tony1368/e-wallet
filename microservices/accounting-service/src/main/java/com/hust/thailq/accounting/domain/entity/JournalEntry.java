package com.hust.thailq.accounting.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "journal_entry")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID transactionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ledger_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Ledger ledger;

    @Column(nullable = false)
    private Long fromWalletId;

    @Column(nullable = false)
    private Long toWalletId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String entryType; // DEBIT or CREDIT

    @Column(length = 255)
    private String description;

    @Column(length = 30)
    private String transactionType;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Boolean erpTransferred = false;

    private Instant erpTransferredAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}

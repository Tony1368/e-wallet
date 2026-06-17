package com.hust.thailq.ewallet.service;

import com.hust.thailq.ewallet.entity.JournalEntry;
import com.hust.thailq.ewallet.entity.Transaction;
import com.hust.thailq.ewallet.entity.Wallet;
import com.hust.thailq.ewallet.repository.JournalEntryRepository;
import com.hust.thailq.ewallet.repository.TransactionRepository;
import com.hust.thailq.ewallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * PaymentService - Monolithic Baseline.
 *
 * TAT CA THAO TAC TRONG CUNG MOT TRANSACTION:
 * 1. SELECT FOR UPDATE (Row Lock) - cho tat ca request khac
 * 2. UPDATE balance - ghi thang PostgreSQL (khong Redis)
 * 3. INSERT transaction - ghi log giao dich
 * 4. INSERT journal_entries - ke toan DONG BO (khong Kafka)
 *
 * KHI 350 REQUESTS DONG THOI:
 * → Lock Queue tich luy (moi request phai cho request truoc hoan tat)
 * → Latency tang luy tien (P99 co the > 5000ms)
 * → Connection pool can kiet → Timeout → Error rate > 0%
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final JournalEntryRepository journalEntryRepository;

    @Transactional
    public Transaction transfer(Long fromWalletId, Long toWalletId, BigDecimal amount, String description) {

        // Step 1: SELECT FOR UPDATE → Row Lock trên PostgreSQL
        // Mọi request khác đến cùng row phải CHỜ ở đây
        Wallet fromWallet = walletRepository.findByIdWithLock(fromWalletId)
                .orElseThrow(() -> new RuntimeException("Source wallet not found: " + fromWalletId));
        Wallet toWallet = walletRepository.findByIdWithLock(toWalletId)
                .orElseThrow(() -> new RuntimeException("Destination wallet not found: " + toWalletId));

        // Step 2: Validate balance
        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Step 3: UPDATE balance trực tiếp trên PostgreSQL (KHÔNG qua Redis)
        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // Step 4: Ghi transaction record vào CÙNG database
        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setDescription(description);
        tx.setFromWalletId(fromWalletId);
        tx.setToWalletId(toWalletId);
        tx.setReferenceNumber(UUID.randomUUID());
        tx.setStatus("SUCCESS");
        transactionRepository.save(tx);

        // Step 5: Ghi kế toán ĐỒNG BỘ vào CÙNG database (KHÔNG qua Kafka)
        JournalEntry debit = new JournalEntry();
        debit.setTransactionId(tx.getReferenceNumber());
        debit.setFromWalletId(fromWalletId);
        debit.setToWalletId(toWalletId);
        debit.setAmount(amount);
        debit.setEntryType("DEBIT");
        journalEntryRepository.save(debit);

        JournalEntry credit = new JournalEntry();
        credit.setTransactionId(tx.getReferenceNumber());
        credit.setFromWalletId(fromWalletId);
        credit.setToWalletId(toWalletId);
        credit.setAmount(amount);
        credit.setEntryType("CREDIT");
        journalEntryRepository.save(credit);

        // → Tất cả 6 thao tác DB trong 1 transaction
        // → Lock chỉ được giải phóng SAU KHI commit
        // → Request tiếp theo mới được bắt đầu

        return tx;
    }

    @Transactional
    public void addFunds(Long walletId, BigDecimal amount, String description) {
        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found: " + walletId));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setDescription(description != null ? description : "Add funds");
        tx.setFromWalletId(walletId);
        tx.setToWalletId(walletId);
        tx.setReferenceNumber(UUID.randomUUID());
        tx.setStatus("SUCCESS");
        transactionRepository.save(tx);

        JournalEntry entry = new JournalEntry();
        entry.setTransactionId(tx.getReferenceNumber());
        entry.setFromWalletId(walletId);
        entry.setToWalletId(walletId);
        entry.setAmount(amount);
        entry.setEntryType("CREDIT");
        journalEntryRepository.save(entry);
    }
}

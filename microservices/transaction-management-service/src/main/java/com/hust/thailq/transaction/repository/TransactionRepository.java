package com.hust.thailq.transaction.repository;

import com.hust.thailq.transaction.domain.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByReferenceNumber(UUID referenceNumber);

    @Query("SELECT t FROM Transaction t WHERE t.fromWalletId = :walletId OR t.toWalletId = :walletId ORDER BY t.createdAt DESC")
    Page<Transaction> findByWalletId(@Param("walletId") Long walletId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.fromWalletId IN :walletIds OR t.toWalletId IN :walletIds ORDER BY t.createdAt DESC")
    Page<Transaction> findByWalletIds(@Param("walletIds") java.util.List<Long> walletIds, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.fromWalletId = :walletId AND t.createdAt >= :since")
    int countByFromWalletIdAndCreatedAtAfter(@Param("walletId") Long walletId, @Param("since") Instant since);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.fromWalletId = :walletId AND t.createdAt >= :since")
    BigDecimal sumAmountByFromWalletIdAndCreatedAtAfter(@Param("walletId") Long walletId, @Param("since") Instant since);

    @Query("SELECT COALESCE(AVG(t.amount), 0) FROM Transaction t WHERE t.fromWalletId = :walletId")
    BigDecimal avgAmountByFromWalletId(@Param("walletId") Long walletId);
}

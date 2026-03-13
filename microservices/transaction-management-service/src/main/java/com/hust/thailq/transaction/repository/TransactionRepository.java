package com.hust.thailq.transaction.repository;

import com.hust.thailq.transaction.domain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByReferenceNumber(UUID referenceNumber);
    
    @Query("SELECT t FROM Transaction t WHERE t.fromWalletId = :walletId OR t.toWalletId = :walletId")
    List<Transaction> findAllByWalletId(@Param("walletId") Long walletId);
}

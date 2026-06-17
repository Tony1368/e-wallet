package com.hust.thailq.ewallet.repository;

import com.hust.thailq.ewallet.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByFromWalletIdOrToWalletId(Long fromId, Long toId, Pageable pageable);
}

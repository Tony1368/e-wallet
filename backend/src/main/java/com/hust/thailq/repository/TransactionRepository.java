package com.hust.thailq.repository;

import com.hust.thailq.domain.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = "SELECT t " +
            "FROM TransactionEntity t " +
            "LEFT JOIN Wallet w ON w.id IN (t.fromWallet.id, t.toWallet.id) " +
            "WHERE w.user.id = :userId " +
            "ORDER BY t.createdAt DESC")
    List<Transaction> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM TransactionEntity t ORDER BY t.createdAt DESC")
    Page<Transaction> findAllOrderByCreatedAtDesc(Pageable pageable);

    Page<Transaction> findAll(Pageable pageable);

    @Query("SELECT t FROM TransactionEntity t " +
           "LEFT JOIN t.fromWallet fw " +
           "LEFT JOIN t.toWallet tw " +
           "LEFT JOIN fw.user fwu " +
           "LEFT JOIN tw.user twu " +
           "WHERE LOWER(fwu.username) = LOWER(:username) OR LOWER(twu.username) = LOWER(:username) " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findAllByUsername(@Param("username") String username);
}

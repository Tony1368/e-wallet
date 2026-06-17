package com.hust.thailq.ewallet.repository;

import com.hust.thailq.ewallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * SELECT FOR UPDATE - tao Row Lock tren PostgreSQL.
     * Day la diem nghen chinh khi nhieu request dong thoi.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdWithLock(Long id);

    Optional<Wallet> findByIban(String iban);
}

package com.hust.thailq.wallet.repository;

import com.hust.thailq.wallet.domain.entity.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByIban(String iban);
    List<Wallet> findByUserId(Long userId);
    boolean existsByIban(String iban);
    Page<Wallet> findByBranchId(Long branchId, Pageable pageable);
    List<Wallet> findByBranchId(Long branchId);
}

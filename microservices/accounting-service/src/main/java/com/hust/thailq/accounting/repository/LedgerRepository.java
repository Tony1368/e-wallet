package com.hust.thailq.accounting.repository;

import com.hust.thailq.accounting.domain.entity.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LedgerRepository extends JpaRepository<Ledger, Long> {
    Optional<Ledger> findByName(String name);
}

package com.hust.thailq.repository;

import com.hust.thailq.domain.entity.FraudRuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FraudRuleConfigRepository extends JpaRepository<FraudRuleConfig, Long> {
    Optional<FraudRuleConfig> findByRuleKey(String ruleKey);
} 
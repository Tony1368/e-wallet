package com.hust.thailq.fraud.repository;

import com.hust.thailq.fraud.domain.entity.FraudRuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FraudRuleConfigRepository extends JpaRepository<FraudRuleConfig, Long> {
    List<FraudRuleConfig> findByEnabledTrue();
}

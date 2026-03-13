package com.hust.thailq.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fraud_rule_config")
public class FraudRuleConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_key", unique = true, nullable = false)
    private String ruleKey;

    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "description")
    private String description;
} 
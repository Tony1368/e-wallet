package com.hust.thailq.controller;

import com.hust.thailq.domain.entity.FraudRuleConfig;
import com.hust.thailq.repository.FraudRuleConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/fraud-config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class FraudRuleConfigController {
    private final FraudRuleConfigRepository configRepository;

    @GetMapping
    public List<FraudRuleConfig> getAll() {
        return configRepository.findAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<FraudRuleConfig> update(@PathVariable Long id, @RequestBody FraudRuleConfig updated) {
        return configRepository.findById(id)
            .map(cfg -> {
                cfg.setValue(updated.getValue());
                cfg.setDescription(updated.getDescription());
                return ResponseEntity.ok(configRepository.save(cfg));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public FraudRuleConfig create(@RequestBody FraudRuleConfig config) {
        return configRepository.save(config);
    }
} 
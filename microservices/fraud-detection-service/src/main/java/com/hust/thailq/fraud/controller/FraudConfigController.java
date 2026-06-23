package com.hust.thailq.fraud.controller;

import com.hust.thailq.fraud.domain.entity.FraudRuleConfig;
import com.hust.thailq.fraud.repository.FraudRuleConfigRepository;
import com.hust.thailq.fraud.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/fraud-config")
@RequiredArgsConstructor
public class FraudConfigController {

    private final FraudRuleConfigRepository ruleConfigRepository;
    private final FraudDetectionService fraudDetectionService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllConfigs() {
        List<FraudRuleConfig> configs = ruleConfigRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (FraudRuleConfig c : configs) {
            // Gioi han so tien
            result.add(row(c.getId(), "max_transaction_amount",
                    "So tien toi da moi giao dich (VND)", c.getMaxTransactionAmount().toPlainString()));
            result.add(row(c.getId(), "max_daily_transactions",
                    "So giao dich toi da moi ngay", String.valueOf(c.getMaxDailyTransactions())));
            result.add(row(c.getId(), "max_daily_amount",
                    "Tong so tien toi da moi ngay (VND)", c.getMaxDailyAmount().toPlainString()));

            // Tan suat giao dich
            result.add(row(c.getId(), "max_transactions_per_minute",
                    "So giao dich toi da trong 1 phut", String.valueOf(c.getMaxTransactionsPerMinute())));
            result.add(row(c.getId(), "velocity_window_seconds",
                    "Khung thoi gian kiem tra tan suat (giay)", String.valueOf(c.getVelocityWindowSeconds())));

            // Vi tri dia ly
            result.add(row(c.getId(), "geo_velocity_minutes",
                    "Thoi gian toi thieu giua 2 vi tri khac nhau (phut)", String.valueOf(c.getGeoVelocityMinutes())));
            result.add(row(c.getId(), "geo_velocity_enabled",
                    "Bat/Tat kiem tra vi tri dia ly", String.valueOf(c.getGeoVelocityEnabled())));

            // Phat hien bat thuong
            result.add(row(c.getId(), "anomaly_amount_multiplier",
                    "He so nhan de phat hien so tien bat thuong (x lan trung binh)", c.getAnomalyAmountMultiplier().toPlainString()));
            result.add(row(c.getId(), "anomaly_enabled",
                    "Bat/Tat phat hien so tien bat thuong", String.valueOf(c.getAnomalyEnabled())));

            // Trang thai chung
            result.add(row(c.getId(), "enabled",
                    "Bat/Tat toan bo quy tac phong chong gian lan", String.valueOf(c.getEnabled())));
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateConfig(@PathVariable Long id, @RequestBody Map<String, String> body) {
        FraudRuleConfig config = ruleConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Config not found: " + id));

        String field = body.get("ruleName");
        String value = body.get("value");

        switch (field) {
            case "max_transaction_amount" -> config.setMaxTransactionAmount(new BigDecimal(value));
            case "max_daily_transactions" -> config.setMaxDailyTransactions(Integer.parseInt(value));
            case "max_daily_amount" -> config.setMaxDailyAmount(new BigDecimal(value));
            case "max_transactions_per_minute" -> config.setMaxTransactionsPerMinute(Integer.parseInt(value));
            case "velocity_window_seconds" -> config.setVelocityWindowSeconds(Integer.parseInt(value));
            case "geo_velocity_minutes" -> config.setGeoVelocityMinutes(Integer.parseInt(value));
            case "geo_velocity_enabled" -> config.setGeoVelocityEnabled(Boolean.parseBoolean(value));
            case "anomaly_amount_multiplier" -> config.setAnomalyAmountMultiplier(new BigDecimal(value));
            case "anomaly_enabled" -> config.setAnomalyEnabled(Boolean.parseBoolean(value));
            case "enabled" -> config.setEnabled(Boolean.parseBoolean(value));
        }

        ruleConfigRepository.save(config);
        fraudDetectionService.invalidateCache();
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> row(Long id, String ruleName, String description, String value) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", id);
        r.put("ruleName", ruleName);
        r.put("description", description);
        r.put("value", value);
        return r;
    }
}

package com.hust.thailq.fraud.service;

import com.hust.thailq.fraud.domain.entity.FraudRuleConfig;
import com.hust.thailq.fraud.dto.FraudCheckRequest;
import com.hust.thailq.fraud.dto.FraudCheckResponse;
import com.hust.thailq.fraud.repository.FraudRuleConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final FraudRuleConfigRepository ruleConfigRepository;

    // Cache config trong memory, refresh mỗi 30 giây
    private final AtomicReference<List<FraudRuleConfig>> cachedRules = new AtomicReference<>();
    private volatile long lastFetchTime = 0;
    private static final long CACHE_TTL_MS = 30_000; // 30 seconds

    private List<FraudRuleConfig> getActiveRules() {
        long now = System.currentTimeMillis();
        if (now - lastFetchTime > CACHE_TTL_MS || cachedRules.get() == null) {
            cachedRules.set(ruleConfigRepository.findByEnabledTrue());
            lastFetchTime = now;
            log.debug("Fraud rules cache refreshed");
        }
        return cachedRules.get();
    }

    public void invalidateCache() {
        cachedRules.set(null);
        lastFetchTime = 0;
        log.info("Fraud rules cache invalidated");
    }

    public FraudCheckResponse checkTransaction(FraudCheckRequest request) {
        List<FraudRuleConfig> rules = getActiveRules();

        for (FraudRuleConfig rule : rules) {

            // 1. Kiem tra so tien vuot gioi han
            if (request.getAmount().compareTo(rule.getMaxTransactionAmount()) > 0) {
                log.warn("FRAUD: Amount {} exceeds max {}", request.getAmount(), rule.getMaxTransactionAmount());
                return new FraudCheckResponse(true,
                        "So tien giao dich vuot gioi han cho phep: " + rule.getMaxTransactionAmount());
            }

            // 2. Kiem tra so luong giao dich trong ngay
            if (request.getDailyTransactionCount() != null
                    && request.getDailyTransactionCount() >= rule.getMaxDailyTransactions()) {
                log.warn("FRAUD: Daily count {} exceeds max {}", request.getDailyTransactionCount(), rule.getMaxDailyTransactions());
                return new FraudCheckResponse(true,
                        "So giao dich trong ngay vuot gioi han: " + rule.getMaxDailyTransactions());
            }

            // 3. Kiem tra tong so tien trong ngay
            if (request.getDailyTotalAmount() != null
                    && request.getDailyTotalAmount().add(request.getAmount()).compareTo(rule.getMaxDailyAmount()) > 0) {
                log.warn("FRAUD: Daily total {} + {} exceeds max {}", request.getDailyTotalAmount(), request.getAmount(), rule.getMaxDailyAmount());
                return new FraudCheckResponse(true,
                        "Tong so tien giao dich trong ngay vuot gioi han: " + rule.getMaxDailyAmount());
            }

            // 4. Kiem tra tan suat giao dich (velocity)
            if (request.getTransactionsInLastMinute() != null
                    && request.getTransactionsInLastMinute() >= rule.getMaxTransactionsPerMinute()) {
                log.warn("FRAUD: Velocity {} tx/min exceeds max {}", request.getTransactionsInLastMinute(), rule.getMaxTransactionsPerMinute());
                return new FraudCheckResponse(true,
                        "Tan suat giao dich bat thuong: " + request.getTransactionsInLastMinute()
                                + " giao dich trong " + rule.getVelocityWindowSeconds() + " giay");
            }

            // 5. Kiem tra vi tri dia ly (geo-velocity)
            if (Boolean.TRUE.equals(rule.getGeoVelocityEnabled())) {
                FraudCheckResponse geoResult = checkGeoVelocity(request, rule);
                if (geoResult.isFraudulent()) {
                    return geoResult;
                }
            }

            // 6. Kiem tra so tien bat thuong (anomaly detection)
            if (Boolean.TRUE.equals(rule.getAnomalyEnabled())) {
                FraudCheckResponse anomalyResult = checkAnomalyAmount(request, rule);
                if (anomalyResult.isFraudulent()) {
                    return anomalyResult;
                }
            }
        }

        return new FraudCheckResponse(false, null);
    }

    /**
     * Kiem tra giao dich o 2 vi tri dia ly khac nhau trong thoi gian ngan.
     * VD: Giao dich tai Ha Noi, 5 phut sau giao dich tai TP.HCM -> gian lan
     */
    private FraudCheckResponse checkGeoVelocity(FraudCheckRequest request, FraudRuleConfig rule) {
        if (request.getCurrentLatitude() == null || request.getLastLatitude() == null) {
            return new FraudCheckResponse(false, null);
        }
        if (request.getMinutesSinceLastTransaction() == null) {
            return new FraudCheckResponse(false, null);
        }

        try {
            double currentLat = Double.parseDouble(request.getCurrentLatitude());
            double currentLon = Double.parseDouble(request.getCurrentLongitude());
            double lastLat = Double.parseDouble(request.getLastLatitude());
            double lastLon = Double.parseDouble(request.getLastLongitude());

            double distanceKm = calculateDistanceKm(currentLat, currentLon, lastLat, lastLon);

            // Neu khoang cach > 50km va thoi gian < geoVelocityMinutes -> nghi ngo
            if (distanceKm > 50 && request.getMinutesSinceLastTransaction() < rule.getGeoVelocityMinutes()) {
                log.warn("FRAUD: Geo-velocity detected. Distance={}km in {} minutes (threshold={}min)",
                        String.format("%.1f", distanceKm),
                        request.getMinutesSinceLastTransaction(),
                        rule.getGeoVelocityMinutes());
                return new FraudCheckResponse(true,
                        "Phat hien giao dich o 2 vi tri cach nhau " + String.format("%.0f", distanceKm)
                                + "km trong " + request.getMinutesSinceLastTransaction() + " phut"
                                + " (gioi han: " + rule.getGeoVelocityMinutes() + " phut)");
            }
        } catch (NumberFormatException e) {
            log.debug("Cannot parse geo coordinates: {}", e.getMessage());
        }

        return new FraudCheckResponse(false, null);
    }

    /**
     * Kiem tra so tien bat thuong so voi lich su giao dich.
     * VD: Trung binh giao dich 500k, dot nhien giao dich 5 trieu -> canh bao
     */
    private FraudCheckResponse checkAnomalyAmount(FraudCheckRequest request, FraudRuleConfig rule) {
        if (request.getAverageTransactionAmount() == null
                || request.getAverageTransactionAmount().compareTo(BigDecimal.ZERO) == 0) {
            return new FraudCheckResponse(false, null);
        }

        BigDecimal threshold = request.getAverageTransactionAmount()
                .multiply(rule.getAnomalyAmountMultiplier());

        if (request.getAmount().compareTo(threshold) > 0) {
            log.warn("FRAUD: Anomaly amount detected. Amount={}, avg={}, multiplier={}, threshold={}",
                    request.getAmount(), request.getAverageTransactionAmount(),
                    rule.getAnomalyAmountMultiplier(), threshold);
            return new FraudCheckResponse(true,
                    "So tien giao dich bat thuong: " + request.getAmount()
                            + " (gap " + rule.getAnomalyAmountMultiplier() + " lan trung binh: "
                            + request.getAverageTransactionAmount() + ")");
        }

        return new FraudCheckResponse(false, null);
    }

    /**
     * Tinh khoang cach giua 2 toa do (Haversine formula)
     */
    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Ban kinh Trai Dat (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}

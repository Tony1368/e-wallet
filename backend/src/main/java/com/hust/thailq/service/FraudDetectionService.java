package com.hust.thailq.service;

import com.hust.thailq.domain.entity.Transaction;
import com.hust.thailq.domain.entity.User;
import com.hust.thailq.domain.entity.UserSession;
import com.hust.thailq.repository.TransactionRepository;
import com.hust.thailq.repository.UserSessionRepository;
import com.hust.thailq.repository.FraudRuleConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {
    private final UserSessionRepository userSessionRepository;
    private final TransactionRepository transactionRepository;
    private final FraudRuleConfigRepository fraudRuleConfigRepository;

    // --- Fraud Rule Interface ---
    public interface FraudRule {
        /**
         * @return null if not fraud, or a reason string if fraud detected
         */
        String check(Transaction tx, User user, List<Transaction> history, UserSession session);
    }

    // --- Rule 1: Rapid Location Change ---
    public static class RapidLocationChangeRule implements FraudRule {
        private final List<UserSession> sessions;
        public RapidLocationChangeRule(List<UserSession> sessions) { this.sessions = sessions; }
        @Override
        public String check(Transaction tx, User user, List<Transaction> history, UserSession session) {
            log.info("=== RapidLocationChangeRule check ===");
            if (sessions == null || sessions.size() < 2) {
                log.info("Not enough sessions for location check (sessions: {})", sessions != null ? sessions.size() : 0);
                return null;
            }
            
            UserSession currentSession = sessions.stream().filter(s -> Boolean.TRUE.equals(s.getIsActive())).findFirst().orElse(null);
            log.info("Current active session: {}", currentSession != null ? 
                String.format("country=%s, loginTime=%s", currentSession.getCountry(), currentSession.getLoginTime()) : "null");
            
            UserSession previousSession = sessions.stream()
                    .filter(s -> s.getLoginTime() != null && s.getCountry() != null && !Objects.equals(s.getCountry(), ""))
                    .filter(s -> currentSession != null && s.getLoginTime().isBefore(currentSession.getLoginTime()))
                    .max(Comparator.comparing(UserSession::getLoginTime))
                    .orElse(null);
            
            log.info("Previous session: {}", previousSession != null ? 
                String.format("country=%s, loginTime=%s", previousSession.getCountry(), previousSession.getLoginTime()) : "null");
            
            if (currentSession == null || previousSession == null) {
                log.info("Missing current or previous session, skipping rule");
                return null;
            }
            
            if (!previousSession.getCountry().equalsIgnoreCase(currentSession.getCountry())) {
                // Ignore if previous or current country is 'Unknown'
                if ("Unknown".equalsIgnoreCase(previousSession.getCountry()) || "Unknown".equalsIgnoreCase(currentSession.getCountry())) {
                    log.info("Ignoring location change involving 'Unknown' country");
                    return null;
                }
                long minutes = Duration.between(previousSession.getLoginTime(), currentSession.getLoginTime()).toMinutes();
                log.info("Location change detected: {} → {} in {} minutes", 
                    previousSession.getCountry(), currentSession.getCountry(), minutes);
                if (minutes <= 30) {
                    String reason = "Rapid location change: " + previousSession.getCountry() + " → " + currentSession.getCountry() + " in " + minutes + " min";
                    log.warn("RULE TRIGGERED: {}", reason);
                    return reason;
                } else {
                    log.info("Location change within acceptable time frame (>30 minutes)");
                }
            } else {
                log.info("No location change detected");
            }
            
            log.info("Rule passed - no rapid location change");
            return null;
        }
    }

    // --- Rule 2: Unusual Amount ---
    public static class UnusualAmountRule implements FraudRule {
        @Override
        public String check(Transaction tx, User user, List<Transaction> history, UserSession session) {
            log.info("=== UnusualAmountRule check ===");
            if (history == null || history.isEmpty()) {
                log.info("No transaction history, skipping rule");
                return null;
            }
            
            BigDecimal avg = history.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(history.size()), 2, java.math.RoundingMode.HALF_UP);
            
            log.info("Transaction amount: {}", tx.getAmount());
            log.info("Average transaction amount: {}", avg);
            log.info("History size: {}", history.size());
            
            BigDecimal threshold = avg.multiply(BigDecimal.valueOf(3));
            log.info("Threshold (3x average): {}", threshold);
            
            if (tx.getAmount().compareTo(threshold) > 0) {
                String reason = "Unusually large amount: " + tx.getAmount() + " (avg: " + avg + ")";
                log.warn("RULE TRIGGERED: {}", reason);
                return reason;
            }
            
            log.info("Rule passed - amount within normal range");
            return null;
        }
    }

    // --- Rule 3: Too Many Transactions ---
    public class TooManyTransactionsRule implements FraudRule {
        @Override
        public String check(Transaction tx, User user, List<Transaction> history, UserSession session) {
            log.info("=== TooManyTransactionsRule check ===");
            if (history == null || history.isEmpty()) {
                log.info("No transaction history, skipping rule");
                return null; // Don't flag first transaction
            }
            
            int maxTx = getMaxTransactionsPerMinute();
            log.info("Max transactions per minute: {}", maxTx);
            
            Instant now = Instant.now();
            Instant oneMinuteAgo = now.minus(1, ChronoUnit.MINUTES);
            log.info("Current time (UTC): {}", now);
            log.info("Checking transactions after (UTC): {}", oneMinuteAgo);
            
            // TEMPORARY FIX: Account for timezone offset (4 hours ahead)
            // This suggests transactions are stored in local time while current time is UTC
            Instant adjustedOneMinuteAgo = oneMinuteAgo.plus(4, ChronoUnit.HOURS);
            log.info("Adjusted time for comparison: {}", adjustedOneMinuteAgo);
            
            long count = history.stream()
                .filter(t -> {
                    // Ensure we're comparing timestamps in the same timezone
                    Instant txTime = t.getCreatedAt();
                    // Use adjusted time for comparison due to timezone offset
                    boolean afterOneMinute = txTime != null && txTime.isAfter(adjustedOneMinuteAgo);
                    
                    long timeDiffMinutes = txTime != null ? Duration.between(txTime, now.plus(4, ChronoUnit.HOURS)).toMinutes() : -1;
                    log.debug("Transaction {}: createdAt={}, afterOneMinute={}, timeDiff={} minutes", 
                        t.getReferenceNumber(), txTime, afterOneMinute, timeDiffMinutes);
                    
                    return afterOneMinute;
                })
                .filter(t -> {
                    boolean isSuccess = t.getStatus() == com.hust.thailq.domain.enums.Status.SUCCESS;
                    log.debug("Transaction {}: status={}, isSuccess={}", 
                        t.getReferenceNumber(), t.getStatus(), isSuccess);
                    return isSuccess;
                })
                .filter(t -> {
                    boolean isNotCurrent = !t.getReferenceNumber().equals(tx.getReferenceNumber());
                    log.debug("Transaction {}: isNotCurrent={}", t.getReferenceNumber(), isNotCurrent);
                    return isNotCurrent;
                })
                .count();
                
            log.info("Transactions in last minute: {} (excluding current transaction)", count);
            
            if (count > maxTx) {
                String reason = "Too many transactions in 1 minute: " + count + " (max allowed: " + maxTx + ")";
                log.warn("RULE TRIGGERED: {}", reason);
                return reason;
            }
            
            log.info("Rule passed - transaction count within limit");
            return null;
        }
    }

    private int getMaxTransactionsPerMinute() {
        return fraudRuleConfigRepository.findByRuleKey("max_transactions_per_minute")
            .map(cfg -> {
                try { return Integer.parseInt(cfg.getValue()); } catch (Exception e) { return 5; }
            })
            .orElse(5);
    }

    // --- Rule 4: New Device/Browser ---
    public static class NewDeviceRule implements FraudRule {
        @Override
        public String check(Transaction tx, User user, List<Transaction> history, UserSession session) {
            // Not implemented: would require storing device/browser history per user
            return null;
        }
    }

    // --- Fraud Detection Engine ---
    public class FraudDetectionEngine {
        private final List<FraudRule> rules;
        public FraudDetectionEngine(List<FraudRule> rules) { this.rules = rules; }
        public List<String> checkAll(Transaction tx, User user, List<Transaction> history, UserSession session) {
            List<String> reasons = new ArrayList<>();
            for (FraudRule rule : rules) {
                String reason = rule.check(tx, user, history, session);
                if (reason != null) reasons.add(reason);
            }
            return reasons;
        }
    }

    /**
     * Main fraud check method: returns list of fraud reasons (empty if not fraud)
     */
    public List<String> checkTransactionFraud(Transaction tx, User user, UserSession session) {
        log.info("=== FRAUD DETECTION START for user {} ===", user.getId());
        log.info("Transaction amount: {}, type: {}, reference: {}", tx.getAmount(), tx.getType(), tx.getReferenceNumber());
        log.info("Current session: country={}, ip={}, loginTime={}", 
            session != null ? session.getCountry() : "null", 
            session != null ? session.getIpAddress() : "null",
            session != null ? session.getLoginTime() : "null");
        
        // Get user transaction history (last 50 for performance)
        List<Transaction> history = transactionRepository.findAllByUserId(user.getId());
        log.info("Transaction history count: {}", history.size());
        if (history.size() > 50) {
            history = history.subList(0, 50);
            log.info("Limited history to last 50 transactions");
        }
        
        // Get all sessions for location rule
        List<UserSession> sessions = userSessionRepository.findByUserIdOrderByLoginTimeDesc(user.getId());
        log.info("User sessions count: {}", sessions.size());
        
        // Log recent transactions for debugging with timezone info
        if (!history.isEmpty()) {
            Transaction lastTx = history.get(0);
            Instant now = Instant.now();
            log.info("Current time (UTC): {}", now);
            log.info("Last transaction: amount={}, createdAt={}, status={}, timeDiff={} minutes", 
                lastTx.getAmount(), lastTx.getCreatedAt(), lastTx.getStatus(),
                lastTx.getCreatedAt() != null ? Duration.between(lastTx.getCreatedAt(), now).toMinutes() : "null");
        }
        
        // Build rules
        List<FraudRule> rules = Arrays.asList(
                new RapidLocationChangeRule(sessions),
                new UnusualAmountRule(),
                new TooManyTransactionsRule(),
                new NewDeviceRule()
        );
        
        FraudDetectionEngine engine = new FraudDetectionEngine(rules);
        List<String> reasons = engine.checkAll(tx, user, history, session);
        
        if (!reasons.isEmpty()) {
            log.warn("FRAUD DETECTED for user {}: {}", user.getId(), String.join("; ", reasons));
        } else {
            log.info("No fraud detected for user {}", user.getId());
        }
        
        log.info("=== FRAUD DETECTION END for user {} ===", user.getId());
        return reasons;
    }
} 
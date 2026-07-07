package com.hust.thailq.wallet.service;

import com.hust.thailq.wallet.domain.entity.Wallet;
import com.hust.thailq.wallet.domain.enums.WalletStatus;
import com.hust.thailq.wallet.dto.request.WalletRequest;
import com.hust.thailq.wallet.dto.response.CommandResponse;
import com.hust.thailq.wallet.dto.response.WalletResponse;
import com.hust.thailq.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String WALLET_BALANCE_KEY_PREFIX = "wallet:balance:";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Transactional(readOnly = true)
    public WalletResponse findById(Long id) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + id));
        WalletResponse response = toResponse(wallet);
        // Return cached balance if available
        BigDecimal cachedBalance = getCachedBalance(id);
        if (cachedBalance != null) {
            response.setBalance(cachedBalance);
        }
        return response;
    }

    @Transactional(readOnly = true)
    public WalletResponse findByIban(String iban) {
        Wallet wallet = walletRepository.findByIban(iban)
                .orElseThrow(() -> new RuntimeException("Wallet not found with iban: " + iban));
        WalletResponse response = toResponse(wallet);
        BigDecimal cachedBalance = getCachedBalance(wallet.getId());
        if (cachedBalance != null) {
            response.setBalance(cachedBalance);
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> findByUserId(Long userId) {
        return walletRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<WalletResponse> findAll(Pageable pageable) {
        return walletRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<WalletResponse> findByBranchId(Long branchId, Pageable pageable) {
        return walletRepository.findByBranchId(branchId, pageable).map(this::toResponse);
    }

    @Transactional
    public CommandResponse create(WalletRequest request) {
        if (walletRepository.existsByIban(request.getIban())) {
            throw new RuntimeException("Wallet already exists with iban: " + request.getIban());
        }

        Wallet wallet = new Wallet();
        wallet.setName(request.getName());
        wallet.setIban(request.getIban());
        wallet.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        wallet.setUserId(request.getUserId());
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setCreatedAt(Instant.now());

        Wallet saved = walletRepository.save(wallet);
        // Cache balance in Redis
        setCachedBalance(saved.getId(), saved.getBalance());
        return CommandResponse.builder().id(saved.getId()).message("Wallet created successfully").build();
    }

    @Transactional
    public CommandResponse update(Long id, WalletRequest request) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + id));

        wallet.setName(request.getName());
        wallet.setBalance(request.getBalance());

        walletRepository.save(wallet);
        setCachedBalance(id, request.getBalance());
        return CommandResponse.builder().id(wallet.getId()).message("Wallet updated successfully").build();
    }

    @Transactional
    public void deleteById(Long id) {
        if (!walletRepository.existsById(id)) {
            throw new RuntimeException("Wallet not found with id: " + id);
        }
        walletRepository.deleteById(id);
        redisTemplate.delete(WALLET_BALANCE_KEY_PREFIX + id);
    }

    @Transactional
    public void updateStatus(Long id, WalletStatus status) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + id));
        wallet.setStatus(status);
        walletRepository.save(wallet);
    }

    /**
     * Debit balance using Redis for high-throughput.
     * Uses DECRBY on Redis first, then async write-behind to PostgreSQL.
     */
    @Transactional
    public void debit(Long walletId, BigDecimal amount) {
        String key = WALLET_BALANCE_KEY_PREFIX + walletId;

        // Ensure balance is loaded into Redis
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            Wallet wallet = walletRepository.findById(walletId)
                    .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + walletId));
            setCachedBalance(walletId, wallet.getBalance());
        }

        // Atomic decrement on Redis
        Long newBalanceCents = redisTemplate.opsForValue()
                .increment(key, amount.negate().multiply(BigDecimal.valueOf(100)).longValue());

        if (newBalanceCents != null && newBalanceCents < 0) {
            // Rollback if insufficient balance
            redisTemplate.opsForValue().increment(key, amount.multiply(BigDecimal.valueOf(100)).longValue());
            throw new RuntimeException("Insufficient balance in wallet: " + walletId);
        }

        log.info("Debited {} from wallet {}, new cached balance (cents): {}", amount, walletId, newBalanceCents);
    }

    /**
     * Credit balance using Redis for high-throughput.
     */
    @Transactional
    public void credit(Long walletId, BigDecimal amount) {
        String key = WALLET_BALANCE_KEY_PREFIX + walletId;

        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            Wallet wallet = walletRepository.findById(walletId)
                    .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + walletId));
            setCachedBalance(walletId, wallet.getBalance());
        }

        redisTemplate.opsForValue().increment(key, amount.multiply(BigDecimal.valueOf(100)).longValue());
        log.info("Credited {} to wallet {}", amount, walletId);
    }

    @Transactional
    public void updateBalance(Long id, BigDecimal newBalance) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + id));
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);
        setCachedBalance(id, newBalance);
    }

    /**
     * Write-behind: periodically sync Redis balances back to PostgreSQL.
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void syncBalancesToDatabase() {
        Set<String> keys = redisTemplate.keys(WALLET_BALANCE_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            try {
                Long walletId = Long.parseLong(key.replace(WALLET_BALANCE_KEY_PREFIX, ""));
                BigDecimal balance = getCachedBalance(walletId);
                if (balance != null) {
                    walletRepository.findById(walletId).ifPresent(wallet -> {
                        wallet.setBalance(balance);
                        walletRepository.save(wallet);
                    });
                }
            } catch (Exception e) {
                log.error("Error syncing balance for key {}: {}", key, e.getMessage());
            }
        }
    }

    private BigDecimal getCachedBalance(Long walletId) {
        String value = redisTemplate.opsForValue().get(WALLET_BALANCE_KEY_PREFIX + walletId);
        if (value != null) {
            return new BigDecimal(value).divide(BigDecimal.valueOf(100));
        }
        return null;
    }

    private void setCachedBalance(Long walletId, BigDecimal balance) {
        long cents = balance.multiply(BigDecimal.valueOf(100)).longValue();
        redisTemplate.opsForValue().set(WALLET_BALANCE_KEY_PREFIX + walletId, String.valueOf(cents));
    }

    /**
     * Read balance directly from PostgreSQL (bypass Redis).
     */
    public Map<String, Object> getDbBalance(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found: " + walletId));
        BigDecimal redisBalance = getCachedBalance(walletId);
        return Map.of(
                "walletId", walletId,
                "dbBalance", wallet.getBalance(),
                "redisBalance", redisBalance != null ? redisBalance : BigDecimal.ZERO,
                "drift", redisBalance != null ? redisBalance.subtract(wallet.getBalance()) : BigDecimal.ZERO
        );
    }

    /**
     * Check drift across all wallets between Redis and PostgreSQL.
     */
    public Map<String, Object> checkDrift() {
        Set<String> keys = redisTemplate.keys(WALLET_BALANCE_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return Map.of("totalDrift", BigDecimal.ZERO, "walletCount", 0);
        }

        BigDecimal totalDrift = BigDecimal.ZERO;
        java.util.List<Map<String, Object>> details = new java.util.ArrayList<>();

        for (String key : keys) {
            try {
                Long walletId = Long.parseLong(key.replace(WALLET_BALANCE_KEY_PREFIX, ""));
                BigDecimal redisBalance = getCachedBalance(walletId);
                Wallet wallet = walletRepository.findById(walletId).orElse(null);
                if (wallet != null && redisBalance != null) {
                    BigDecimal drift = redisBalance.subtract(wallet.getBalance());
                    totalDrift = totalDrift.add(drift.abs());
                    details.add(Map.of(
                            "walletId", walletId,
                            "redis", redisBalance,
                            "db", wallet.getBalance(),
                            "drift", drift
                    ));
                }
            } catch (Exception e) {
                log.error("Drift check error for key {}: {}", key, e.getMessage());
            }
        }

        return Map.of(
                "totalDrift", totalDrift,
                "walletCount", details.size(),
                "details", details,
                "timestamp", Instant.now().toString()
        );
    }

    private WalletResponse toResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setIban(wallet.getIban());
        response.setName(wallet.getName());
        response.setBalance(wallet.getBalance());
        response.setUserId(wallet.getUserId());
        response.setCreatedAt(FORMATTER.format(wallet.getCreatedAt()));
        response.setBankInfo(wallet.getBankInfo());
        response.setStatus(wallet.getStatus());
        response.setBranchId(wallet.getBranchId());
        return response;
    }
}

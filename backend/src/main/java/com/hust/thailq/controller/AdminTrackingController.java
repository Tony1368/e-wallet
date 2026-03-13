package com.hust.thailq.controller;

import com.hust.thailq.dto.response.UserActivityResponse;
import com.hust.thailq.dto.response.UserSessionResponse;
import com.hust.thailq.service.UserTrackingDataService;
import com.hust.thailq.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.hust.thailq.security.UserDetailsImpl;
import com.hust.thailq.repository.UserRepository;
import com.hust.thailq.domain.entity.User;
import com.hust.thailq.domain.entity.UserSession;
import com.hust.thailq.repository.UserSessionRepository;
import com.hust.thailq.domain.entity.Transaction;

/**
 * Controller for admin access to user tracking data.
 */
@RestController
@RequestMapping("/api/v1/admin/tracking")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminTrackingController {

    private final UserTrackingDataService userTrackingDataService;
    private final FraudDetectionService fraudDetectionService;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;

    /**
     * Get all user sessions with pagination
     */
    @GetMapping("/sessions")
    public ResponseEntity<Page<UserSessionResponse>> getAllUserSessions(Pageable pageable) {
        final Page<UserSessionResponse> response = userTrackingDataService.getAllUserSessions(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user sessions by user ID
     */
    @GetMapping("/sessions/users/{userId}")
    public ResponseEntity<List<UserSessionResponse>> getUserSessionsByUserId(@PathVariable Long userId) {
        final List<UserSessionResponse> response = userTrackingDataService.getUserSessionsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user sessions by username
     */
    @GetMapping("/sessions/username/{username}")
    public ResponseEntity<List<UserSessionResponse>> getUserSessionsByUsername(@PathVariable String username) {
        final List<UserSessionResponse> response = userTrackingDataService.getUserSessionsByUsername(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active user sessions
     */
    @GetMapping("/sessions/active")
    public ResponseEntity<Page<UserSessionResponse>> getActiveUserSessions(Pageable pageable) {
        final Page<UserSessionResponse> response = userTrackingDataService.getActiveUserSessions(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user sessions by date range
     */
    @GetMapping("/sessions/users/{userId}/date-range")
    public ResponseEntity<List<UserSessionResponse>> getUserSessionsByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {
        final List<UserSessionResponse> response = userTrackingDataService.getUserSessionsByDateRange(userId, startTime, endTime);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all user activities with pagination
     */
    @GetMapping("/activities")
    public ResponseEntity<Page<UserActivityResponse>> getAllUserActivities(Pageable pageable) {
        final Page<UserActivityResponse> response = userTrackingDataService.getAllUserActivities(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user activities by user ID
     */
    @GetMapping("/activities/users/{userId}")
    public ResponseEntity<Page<UserActivityResponse>> getUserActivitiesByUserId(
            @PathVariable Long userId, 
            Pageable pageable) {
        final Page<UserActivityResponse> response = userTrackingDataService.getUserActivitiesByUserId(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user activities by username
     */
    @GetMapping("/activities/username/{username}")
    public ResponseEntity<Page<UserActivityResponse>> getUserActivitiesByUsername(
            @PathVariable String username, 
            Pageable pageable) {
        final Page<UserActivityResponse> response = userTrackingDataService.getUserActivitiesByUsername(username, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get financial activities (TRANSFER, WITHDRAW, ADD_FUNDS)
     */
    @GetMapping("/activities/financial")
    public ResponseEntity<Page<UserActivityResponse>> getFinancialActivities(Pageable pageable) {
        final Page<UserActivityResponse> response = userTrackingDataService.getFinancialActivities(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get financial activities by user ID
     */
    @GetMapping("/activities/financial/users/{userId}")
    public ResponseEntity<Page<UserActivityResponse>> getFinancialActivitiesByUserId(
            @PathVariable Long userId, 
            Pageable pageable) {
        final Page<UserActivityResponse> response = userTrackingDataService.getFinancialActivitiesByUserId(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user activities by date range
     */
    @GetMapping("/activities/users/{userId}/date-range")
    public ResponseEntity<List<UserActivityResponse>> getUserActivitiesByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {
        final List<UserActivityResponse> response = userTrackingDataService.getUserActivitiesByDateRange(userId, startTime, endTime);
        return ResponseEntity.ok(response);
    }

    /**
     * Get login activities for a user
     */
    @GetMapping("/activities/users/{userId}/login")
    public ResponseEntity<List<UserActivityResponse>> getLoginActivities(@PathVariable Long userId) {
        final List<UserActivityResponse> response = userTrackingDataService.getLoginActivities(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get transfer activities for a user
     */
    @GetMapping("/activities/users/{userId}/transfer")
    public ResponseEntity<List<UserActivityResponse>> getTransferActivities(@PathVariable Long userId) {
        final List<UserActivityResponse> response = userTrackingDataService.getTransferActivities(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get withdrawal activities for a user
     */
    @GetMapping("/activities/users/{userId}/withdraw")
    public ResponseEntity<List<UserActivityResponse>> getWithdrawalActivities(@PathVariable Long userId) {
        final List<UserActivityResponse> response = userTrackingDataService.getWithdrawalActivities(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get add funds activities for a user
     */
    @GetMapping("/activities/users/{userId}/add-funds")
    public ResponseEntity<List<UserActivityResponse>> getAddFundsActivities(@PathVariable Long userId) {
        final List<UserActivityResponse> response = userTrackingDataService.getAddFundsActivities(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint to simulate fraud detection scenarios
     * This is for testing purposes only
     */
    @GetMapping("/test-fraud-detection")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testFraudDetection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                User user = userRepository.findById(userDetails.getId()).orElse(null);
                
                if (user != null) {
                    // Test fraud detection with the new rule-based engine
                    List<Map<String, Object>> scenarios = new ArrayList<>();
                    List<UserSession> sessions = userSessionRepository.findByUserIdOrderByLoginTimeDesc(user.getId());
                    // For demo, just use the most recent session and a dummy transaction
                    UserSession currentSession = sessions.isEmpty() ? null : sessions.get(0);
                    Transaction dummyTx = new Transaction();
                    dummyTx.setAmount(new java.math.BigDecimal("1000000"));
                    dummyTx.setCreatedAt(java.time.Instant.now());
                    List<String> fraudReasons = fraudDetectionService.checkTransactionFraud(dummyTx, user, currentSession);
                    Map<String, Object> scenario = new HashMap<>();
                    scenario.put("scenario", "Rule-based fraud detection");
                    scenario.put("fraudDetected", !fraudReasons.isEmpty());
                    scenario.put("reasons", fraudReasons);
                    scenarios.add(scenario);
                    
                    // Get session information for debugging
                    List<Map<String, Object>> sessionInfo = new ArrayList<>();
                    for (UserSession session : sessions) {
                        Map<String, Object> sessionData = new HashMap<>();
                        sessionData.put("sessionId", session.getSessionId());
                        sessionData.put("country", session.getCountry());
                        sessionData.put("ipAddress", session.getIpAddress());
                        sessionData.put("loginTime", session.getLoginTime());
                        sessionData.put("isActive", session.getIsActive());
                        sessionInfo.add(sessionData);
                    }
                    
                    result.put("userId", user.getId());
                    result.put("username", user.getUsername());
                    result.put("scenarios", scenarios);
                    result.put("sessions", sessionInfo);
                    result.put("message", "Fraud detection test completed");
                    
                    return ResponseEntity.ok(result);
                }
            }
            
            result.put("error", "User not found");
            return ResponseEntity.badRequest().body(result);
            
        } catch (Exception e) {
            result.put("error", "Error testing fraud detection: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
} 
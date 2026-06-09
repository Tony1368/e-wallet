package com.hust.thailq.user.controller;

import com.hust.thailq.user.dto.response.UserActivityResponse;
import com.hust.thailq.user.dto.response.UserSessionResponse;
import com.hust.thailq.user.service.UserTrackingDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/tracking")
@RequiredArgsConstructor
public class UserTrackingController {

    private final UserTrackingDataService trackingDataService;

    // Sessions
    @GetMapping("/sessions")
    public ResponseEntity<Page<UserSessionResponse>> getAllSessions(Pageable pageable) {
        return ResponseEntity.ok(trackingDataService.getAllUserSessions(pageable));
    }

    @GetMapping("/sessions/users/{userId}")
    public ResponseEntity<List<UserSessionResponse>> getSessionsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(trackingDataService.getUserSessionsByUserId(userId));
    }

    @GetMapping("/sessions/username/{username}")
    public ResponseEntity<List<UserSessionResponse>> getSessionsByUsername(@PathVariable String username) {
        return ResponseEntity.ok(trackingDataService.getUserSessionsByUsername(username));
    }

    @GetMapping("/sessions/active")
    public ResponseEntity<Page<UserSessionResponse>> getActiveSessions(Pageable pageable) {
        return ResponseEntity.ok(trackingDataService.getActiveUserSessions(pageable));
    }

    // Activities
    @GetMapping("/activities")
    public ResponseEntity<Page<UserActivityResponse>> getAllActivities(Pageable pageable) {
        return ResponseEntity.ok(trackingDataService.getAllUserActivities(pageable));
    }

    @GetMapping("/activities/users/{userId}")
    public ResponseEntity<Page<UserActivityResponse>> getActivitiesByUserId(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(trackingDataService.getUserActivitiesByUserId(userId, pageable));
    }

    @GetMapping("/activities/username/{username}")
    public ResponseEntity<Page<UserActivityResponse>> getActivitiesByUsername(@PathVariable String username, Pageable pageable) {
        return ResponseEntity.ok(trackingDataService.getUserActivitiesByUsername(username, pageable));
    }

    @GetMapping("/activities/financial")
    public ResponseEntity<Page<UserActivityResponse>> getFinancialActivities(Pageable pageable) {
        return ResponseEntity.ok(trackingDataService.getFinancialActivities(pageable));
    }

    @GetMapping("/activities/financial/users/{userId}")
    public ResponseEntity<Page<UserActivityResponse>> getFinancialActivitiesByUserId(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(trackingDataService.getFinancialActivitiesByUserId(userId, pageable));
    }

    @GetMapping("/activities/users/{userId}/login")
    public ResponseEntity<List<UserActivityResponse>> getLoginActivities(@PathVariable Long userId) {
        return ResponseEntity.ok(trackingDataService.getLoginActivities(userId));
    }

    @GetMapping("/activities/users/{userId}/transfer")
    public ResponseEntity<List<UserActivityResponse>> getTransferActivities(@PathVariable Long userId) {
        return ResponseEntity.ok(trackingDataService.getTransferActivities(userId));
    }

    @GetMapping("/activities/users/{userId}/withdraw")
    public ResponseEntity<List<UserActivityResponse>> getWithdrawalActivities(@PathVariable Long userId) {
        return ResponseEntity.ok(trackingDataService.getWithdrawalActivities(userId));
    }

    @GetMapping("/activities/users/{userId}/add-funds")
    public ResponseEntity<List<UserActivityResponse>> getAddFundsActivities(@PathVariable Long userId) {
        return ResponseEntity.ok(trackingDataService.getAddFundsActivities(userId));
    }
}

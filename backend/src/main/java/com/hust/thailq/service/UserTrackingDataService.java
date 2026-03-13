package com.hust.thailq.service;

import com.hust.thailq.domain.entity.UserActivity;
import com.hust.thailq.domain.entity.UserSession;
import com.hust.thailq.dto.mapper.UserActivityResponseMapper;
import com.hust.thailq.dto.mapper.UserSessionResponseMapper;
import com.hust.thailq.dto.response.UserActivityResponse;
import com.hust.thailq.dto.response.UserSessionResponse;
import com.hust.thailq.exception.NoSuchElementFoundException;
import com.hust.thailq.repository.UserActivityRepository;
import com.hust.thailq.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTrackingDataService {

    private final UserSessionRepository userSessionRepository;
    private final UserActivityRepository userActivityRepository;
    private final UserSessionResponseMapper userSessionResponseMapper;
    private final UserActivityResponseMapper userActivityResponseMapper;

    /**
     * Get all user sessions with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserSessionResponse> getAllUserSessions(Pageable pageable) {
        final Page<UserSession> sessions = userSessionRepository.findAllOrderByLoginTimeDesc(pageable);
        return sessions.map(userSessionResponseMapper::toDto);
    }

    /**
     * Get user sessions by user ID
     */
    @Transactional(readOnly = true)
    public List<UserSessionResponse> getUserSessionsByUserId(Long userId) {
        final List<UserSession> sessions = userSessionRepository.findByUserIdOrderByLoginTimeDesc(userId);
        return sessions.stream()
                .map(userSessionResponseMapper::toDto)
                .toList();
    }

    /**
     * Get user sessions by username
     */
    @Transactional(readOnly = true)
    public List<UserSessionResponse> getUserSessionsByUsername(String username) {
        final List<UserSession> sessions = userSessionRepository.findByUserUsernameOrderByLoginTimeDesc(username);
        return sessions.stream()
                .map(userSessionResponseMapper::toDto)
                .toList();
    }

    /**
     * Get active user sessions
     */
    @Transactional(readOnly = true)
    public Page<UserSessionResponse> getActiveUserSessions(Pageable pageable) {
        final Page<UserSession> sessions = userSessionRepository.findAllActiveSessions(pageable);
        return sessions.map(userSessionResponseMapper::toDto);
    }

    /**
     * Get user sessions by date range
     */
    @Transactional(readOnly = true)
    public List<UserSessionResponse> getUserSessionsByDateRange(Long userId, Instant startTime, Instant endTime) {
        final List<UserSession> sessions = userSessionRepository.findByUserIdAndLoginTimeBetween(userId, startTime, endTime);
        return sessions.stream()
                .map(userSessionResponseMapper::toDto)
                .toList();
    }

    /**
     * Get all user activities with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserActivityResponse> getAllUserActivities(Pageable pageable) {
        final Page<UserActivity> activities = userActivityRepository.findAllOrderByActivityTimeDesc(pageable);
        return activities.map(userActivityResponseMapper::toDto);
    }

    /**
     * Get user activities by user ID
     */
    @Transactional(readOnly = true)
    public Page<UserActivityResponse> getUserActivitiesByUserId(Long userId, Pageable pageable) {
        final Page<UserActivity> activities = userActivityRepository.findByUserIdOrderByActivityTimeDesc(userId, pageable);
        return activities.map(userActivityResponseMapper::toDto);
    }

    /**
     * Get user activities by username
     */
    @Transactional(readOnly = true)
    public Page<UserActivityResponse> getUserActivitiesByUsername(String username, Pageable pageable) {
        final Page<UserActivity> activities = userActivityRepository.findByUserUsernameOrderByActivityTimeDesc(username, pageable);
        return activities.map(userActivityResponseMapper::toDto);
    }

    /**
     * Get user activities by activity type
     */
    @Transactional(readOnly = true)
    public List<UserActivityResponse> getUserActivitiesByType(Long userId, String activityType) {
        final List<UserActivity> activities = userActivityRepository.findByUserIdAndActivityTypeOrderByActivityTimeDesc(userId, activityType);
        return activities.stream()
                .map(userActivityResponseMapper::toDto)
                .toList();
    }

    /**
     * Get financial activities (TRANSFER, WITHDRAW, ADD_FUNDS)
     */
    @Transactional(readOnly = true)
    public Page<UserActivityResponse> getFinancialActivities(Pageable pageable) {
        final Page<UserActivity> activities = userActivityRepository.findAllFinancialActivities(pageable);
        return activities.map(userActivityResponseMapper::toDto);
    }

    /**
     * Get financial activities by user ID
     */
    @Transactional(readOnly = true)
    public Page<UserActivityResponse> getFinancialActivitiesByUserId(Long userId, Pageable pageable) {
        final Page<UserActivity> activities = userActivityRepository.findFinancialActivitiesByUserId(userId, pageable);
        return activities.map(userActivityResponseMapper::toDto);
    }

    /**
     * Get user activities by date range
     */
    @Transactional(readOnly = true)
    public List<UserActivityResponse> getUserActivitiesByDateRange(Long userId, Instant startTime, Instant endTime) {
        final List<UserActivity> activities = userActivityRepository.findByUserIdAndActivityTimeBetween(userId, startTime, endTime);
        return activities.stream()
                .map(userActivityResponseMapper::toDto)
                .toList();
    }

    /**
     * Get login activities for a user
     */
    @Transactional(readOnly = true)
    public List<UserActivityResponse> getLoginActivities(Long userId) {
        return getUserActivitiesByType(userId, "LOGIN");
    }

    /**
     * Get transfer activities for a user
     */
    @Transactional(readOnly = true)
    public List<UserActivityResponse> getTransferActivities(Long userId) {
        return getUserActivitiesByType(userId, "TRANSFER");
    }

    /**
     * Get withdrawal activities for a user
     */
    @Transactional(readOnly = true)
    public List<UserActivityResponse> getWithdrawalActivities(Long userId) {
        return getUserActivitiesByType(userId, "WITHDRAW");
    }

    /**
     * Get add funds activities for a user
     */
    @Transactional(readOnly = true)
    public List<UserActivityResponse> getAddFundsActivities(Long userId) {
        return getUserActivitiesByType(userId, "ADD_FUNDS");
    }
} 
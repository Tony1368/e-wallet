package com.hust.thailq.service;

import com.hust.thailq.domain.entity.User;
import com.hust.thailq.domain.entity.UserActivity;
import com.hust.thailq.domain.entity.UserSession;
import com.hust.thailq.repository.UserActivityRepository;
import com.hust.thailq.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTrackingService {

    private final UserSessionRepository userSessionRepository;
    private final UserActivityRepository userActivityRepository;
    private final ClientInfoService clientInfoService;
    private final GeolocationService geolocationService;
    private final ReverseGeocodingService reverseGeocodingService;

    @Transactional
    public UserSession createLoginSession(User user, Double latitude, Double longitude, String ipAddressFromRequest) {
        try {
            log.info("[Session] Creating login session for user {}. Lat: {}, Lon: {}, IP from request: {}", user.getUsername(), latitude, longitude, ipAddressFromRequest);
            String sessionId = UUID.randomUUID().toString();
            String ipAddress = (ipAddressFromRequest != null && !ipAddressFromRequest.isEmpty())
                ? ipAddressFromRequest
                : clientInfoService.getClientIpAddress();
            String userAgent = clientInfoService.getUserAgent();
            Map<String, String> deviceInfo = clientInfoService.parseUserAgent(userAgent);

            Map<String, String> locationInfo;
            if (latitude != null && longitude != null) {
                locationInfo = reverseGeocodingService.reverseGeocode(latitude, longitude);
                locationInfo.put("latitude", String.valueOf(latitude));
                locationInfo.put("longitude", String.valueOf(longitude));
            } else {
                locationInfo = geolocationService.getLocationFromIp(ipAddress);
            }

            log.info("[Session] Location info for user {}: {}", user.getUsername(), locationInfo);

            UserSession session = new UserSession();
            session.setSessionId(sessionId);
            session.setUser(user);
            session.setIpAddress(ipAddress);
            session.setUserAgent(userAgent);
            session.setBrowser(deviceInfo.get("browser"));
            session.setOperatingSystem(deviceInfo.get("operatingSystem"));
            session.setDeviceType(deviceInfo.get("deviceType"));
            session.setIsActive(true);

            // Set login time to now to avoid null
            session.setLoginTime(Instant.now());

            // Set geolocation information
            session.setCountry(locationInfo.get("country"));
            session.setCity(locationInfo.get("city"));
            session.setRegion(locationInfo.get("region"));
            session.setLatitude(locationInfo.get("latitude"));
            session.setLongitude(locationInfo.get("longitude"));
            session.setTimezone(locationInfo.get("timezone"));

            UserSession savedSession = userSessionRepository.save(session);
            log.info("[Session] Created login session for user {} with session ID: {} (lat: {}, lon: {}, country: {}, ip: {})", user.getUsername(), sessionId, latitude, longitude, session.getCountry(), ipAddress);

            return savedSession;
        } catch (Exception e) {
            log.error("Error creating login session for user {}: {}", user.getUsername(), e.getMessage());
            return null;
        }
    }

    @Transactional
    public void endLoginSession(String sessionId) {
        try {
            userSessionRepository.endSessionNow(sessionId);
            log.info("Ended login session: {} (logout_time set by DB)", sessionId);
        } catch (Exception e) {
            log.error("Error ending login session {}: {}", sessionId, e.getMessage());
        }
    }

    @Transactional
    public void trackActivity(User user, String activityType, String description, BigDecimal amount, 
                            String fromWalletIban, String toWalletIban, UserSession session, boolean isSuccessful, String errorMessage) {
        try {
            String ipAddress = clientInfoService.getClientIpAddress();
            String userAgent = clientInfoService.getUserAgent();
            Map<String, String> deviceInfo = clientInfoService.parseUserAgent(userAgent);
            Map<String, String> locationInfo = geolocationService.getLocationFromIp(ipAddress);

            UserActivity activity = new UserActivity();
            activity.setUser(user);
            activity.setSession(session);
            activity.setActivityType(activityType);
            activity.setDescription(description);
            activity.setAmount(amount);
            activity.setFromWalletIban(fromWalletIban);
            activity.setToWalletIban(toWalletIban);
            activity.setIpAddress(ipAddress);
            activity.setUserAgent(userAgent);
            activity.setBrowser(deviceInfo.get("browser"));
            activity.setOperatingSystem(deviceInfo.get("operatingSystem"));
            activity.setDeviceType(deviceInfo.get("deviceType"));
            activity.setIsSuccessful(isSuccessful);
            activity.setErrorMessage(errorMessage);

            // Set geolocation information
            activity.setCountry(locationInfo.get("country"));
            activity.setCity(locationInfo.get("city"));
            activity.setRegion(locationInfo.get("region"));
            activity.setLatitude(locationInfo.get("latitude"));
            activity.setLongitude(locationInfo.get("longitude"));
            activity.setTimezone(locationInfo.get("timezone"));

            userActivityRepository.save(activity);
            log.info("Tracked activity {} for user {}: {}", activityType, user.getUsername(), description);
        } catch (Exception e) {
            log.error("Error tracking activity for user {}: {}", user.getUsername(), e.getMessage());
        }
    }

    @Transactional
    public void trackLoginActivity(User user, UserSession session, boolean isSuccessful, String errorMessage) {
        trackActivityWithSessionInfo(user, "LOGIN", "User login attempt", null, null, null, session, isSuccessful, errorMessage);
    }

    @Transactional
    public void trackTransferActivity(User user, BigDecimal amount, String fromWalletIban, String toWalletIban, 
                                    UserSession session, boolean isSuccessful, String errorMessage) {
        trackActivity(user, "TRANSFER", "Wallet to wallet transfer", amount, fromWalletIban, toWalletIban, 
                     session, isSuccessful, errorMessage);
    }

    @Transactional
    public void trackWithdrawActivity(User user, BigDecimal amount, String fromWalletIban, 
                                    UserSession session, boolean isSuccessful, String errorMessage) {
        trackActivity(user, "WITHDRAW", "Withdraw funds from wallet", amount, fromWalletIban, null, 
                     session, isSuccessful, errorMessage);
    }

    @Transactional
    public void trackAddFundsActivity(User user, BigDecimal amount, String toWalletIban, 
                                    UserSession session, boolean isSuccessful, String errorMessage) {
        trackActivity(user, "ADD_FUNDS", "Add funds to wallet", amount, null, toWalletIban, 
                     session, isSuccessful, errorMessage);
    }

    @Transactional
    public UserSession getCurrentActiveSession(Long userId) {
        try {
            return userSessionRepository.findByUserIdAndIsActiveTrue(userId)
                    .stream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error getting current active session for user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    @Transactional
    public void trackLogoutActivity(User user, UserSession session, boolean isSuccessful, String errorMessage) {
        trackActivityWithSessionInfo(user, "LOGOUT", "User logout", null, null, null, session, isSuccessful, errorMessage);
    }

    /**
     * Checks if the current IP/location has changed and creates a new session if needed.
     * This method should be called before any transaction to ensure proper fraud detection.
     */
    @Transactional
    public UserSession checkAndUpdateSessionForLocationChange(User user) {
        try {
            UserSession currentSession = getCurrentActiveSession(user.getId());
            if (currentSession == null) {
                log.info("[Session] No active session found for user {}, creating new session", user.getId());
                return createLoginSession(user, null, null, null);
            }

            // Get current IP and location
            String currentIp = clientInfoService.getClientIpAddress();
            Map<String, String> currentLocation = geolocationService.getLocationFromIp(currentIp);
            String currentCountry = currentLocation.get("country");

            // Check if IP or country has changed
            boolean ipChanged = !currentIp.equals(currentSession.getIpAddress());
            boolean countryChanged = !currentCountry.equals(currentSession.getCountry());

            log.info("[Session] Checking location change for user {}: old IP={}, new IP={}, old country={}, new country={}, ipChanged={}, countryChanged={}",
                user.getId(), currentSession.getIpAddress(), currentIp, currentSession.getCountry(), currentCountry, ipChanged, countryChanged);

            if (ipChanged || countryChanged) {
                log.warn("[Session] Location change detected for user {}: IP changed={}, country changed={}, old IP={}, old country={}, new IP={}, new country={}",
                        user.getId(), ipChanged, countryChanged, currentSession.getIpAddress(), currentSession.getCountry(), currentIp, currentCountry);

                // End the current session
                endLoginSession(currentSession.getSessionId());

                // Create a new session with the new location
                UserSession newSession = createLoginSession(user, null, null, currentIp);
                
                log.info("[Session] Created new session for user {} due to location change: session ID={}", user.getId(), newSession.getSessionId());
                return newSession;
            }

            return currentSession;
        } catch (Exception e) {
            log.error("Error checking/updating session for location change for user {}: {}", user.getId(), e.getMessage());
            return getCurrentActiveSession(user.getId());
        }
    }

    @Transactional
    public void trackActivityWithSessionInfo(User user, String activityType, String description, BigDecimal amount,
                                             String fromWalletIban, String toWalletIban, UserSession session,
                                             boolean isSuccessful, String errorMessage) {
        try {
            UserActivity activity = new UserActivity();
            activity.setUser(user);
            activity.setSession(session);
            activity.setActivityType(activityType);
            activity.setDescription(description);
            activity.setAmount(amount);
            activity.setFromWalletIban(fromWalletIban);
            activity.setToWalletIban(toWalletIban);
            activity.setIpAddress(session != null ? session.getIpAddress() : null);
            activity.setUserAgent(session != null ? session.getUserAgent() : null);
            activity.setBrowser(session != null ? session.getBrowser() : null);
            activity.setOperatingSystem(session != null ? session.getOperatingSystem() : null);
            activity.setDeviceType(session != null ? session.getDeviceType() : null);
            activity.setIsSuccessful(isSuccessful);
            activity.setErrorMessage(errorMessage);

            // Use session's geolocation info
            activity.setCountry(session != null ? session.getCountry() : null);
            activity.setCity(session != null ? session.getCity() : null);
            activity.setRegion(session != null ? session.getRegion() : null);
            activity.setLatitude(session != null ? session.getLatitude() : null);
            activity.setLongitude(session != null ? session.getLongitude() : null);
            activity.setTimezone(session != null ? session.getTimezone() : null);

            userActivityRepository.save(activity);
            log.info("Tracked activity {} for user {}: {}", activityType, user.getUsername(), description);
        } catch (Exception e) {
            log.error("Error tracking activity for user {}: {}", user.getUsername(), e.getMessage());
        }
    }
} 
package com.hust.thailq.user.service;

import com.hust.thailq.user.config.MessageSourceConfig;
import com.hust.thailq.user.domain.entity.User;
import com.hust.thailq.user.domain.entity.UserSession;
import com.hust.thailq.user.dto.mapper.SignupRequestMapper;
import com.hust.thailq.user.dto.request.LoginRequest;
import com.hust.thailq.user.dto.request.SignupRequest;
import com.hust.thailq.dto.CommandResponse;
import com.hust.thailq.user.dto.response.JwtResponse;
import com.hust.thailq.exception.ElementAlreadyExistsException;
import com.hust.thailq.user.repository.UserRepository;
import com.hust.thailq.user.security.JwtUtils;
import com.hust.thailq.user.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

import static com.hust.thailq.common.MessageKeys.*;

/**
 * Service used for Authentication related operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MessageSourceConfig messageConfig;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final SignupRequestMapper signupRequestMapper;
    private final UserTrackingService userTrackingService;
    // FraudDetectionService removed - not needed in User Service

    /**
     * Authenticates users by their credentials.
     *
     * @param request
     * @return JwtResponse
     */
    public JwtResponse login(LoginRequest request) {
        UserSession session = null;
        try {
            final Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword().trim()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            final UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            final List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .toList();

            // Get user entity for tracking
            User user = userRepository.findById(userDetails.getId()).orElse(null);
            if (user != null) {
                // Create login session and track activity
                session = userTrackingService.createLoginSession(user, request.getLatitude(), request.getLongitude(), request.getIpAddress());
                // Fraud detection for login removed (method no longer exists)
                userTrackingService.trackLoginActivity(user, session, true, null);
            }

            log.info(messageConfig.getMessage(INFO_USER_LOGIN, request.getUsername()));
            return JwtResponse
                    .builder()
                    .token(jwt)
                    .id(userDetails.getId())
                    .username(userDetails.getUsername())
                    .firstName(userDetails.getFirstName())
                    .lastName(userDetails.getLastName())
                    .roles(roles)
                    .branchId(user != null ? user.getBranchId() : null)
                    .build();
        } catch (Exception e) {
            // Track failed login attempt
            try {
                User user = userRepository.findByUsername(request.getUsername().trim()).orElse(null);
                if (user != null) {
                    session = userTrackingService.createLoginSession(user, null, null, request.getIpAddress());
                    userTrackingService.trackLoginActivity(user, session, false, e.getMessage());
                }
            } catch (Exception trackingException) {
                log.error("Error tracking failed login: {}", trackingException.getMessage());
            }
            throw e;
        }
    }

    /**
     * Registers a user by provided credentials and user info.
     *
     * @param request
     * @return id of the registered user
     */
    public CommandResponse signup(SignupRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername().trim()))
            throw new ElementAlreadyExistsException(messageConfig.getMessage(ERROR_USERNAME_EXISTS));
        if (userRepository.existsByEmailIgnoreCase(request.getEmail().trim()))
            throw new ElementAlreadyExistsException(messageConfig.getMessage(ERROR_EMAIL_EXISTS));

        final User user = signupRequestMapper.toEntity(request);
        userRepository.save(user);
        log.info(messageConfig.getMessage(INFO_USER_CREATED, user.getUsername()));
        return CommandResponse.builder().id(user.getId()).build();
    }

    /**
     * Logs out the current user and tracks the logout activity.
     *
     * @return CommandResponse
     */
    public CommandResponse logout() {
        try {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
                final UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                final User user = userRepository.findById(userDetails.getId()).orElse(null);
                
                if (user != null) {
                    // Get the current active session for the user
                    final UserSession currentSession = userTrackingService.getCurrentActiveSession(user.getId());
                    
                    if (currentSession != null) {
                        // End the session and track logout activity
                        userTrackingService.endLoginSession(currentSession.getSessionId());
                        userTrackingService.trackLogoutActivity(user, currentSession, true, null);
                        log.info(messageConfig.getMessage(INFO_USER_LOGOUT, user.getUsername()));
                    }
                }
            }
            
            // Clear the security context
            SecurityContextHolder.clearContext();
            
            return CommandResponse.builder().id(1L).build(); // Success response
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            throw e;
        }
    }
}

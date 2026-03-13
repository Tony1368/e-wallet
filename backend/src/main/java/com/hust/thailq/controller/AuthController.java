package com.hust.thailq.controller;

import com.hust.thailq.dto.request.LoginRequest;
import com.hust.thailq.dto.request.SignupRequest;
import com.hust.thailq.dto.response.CommandResponse;
import com.hust.thailq.dto.response.JwtResponse;
import com.hust.thailq.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class    AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    /**
     * Authenticates users by their credentials.
     *
     * @param request
     * @return JwtResponse
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        final JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Registers users using their credentials and user info.
     *
     * @param request
     * @return id of the registered user
     */
    @PostMapping("/signup")
    public ResponseEntity<CommandResponse> signup(@Valid @RequestBody SignupRequest request) {
        final CommandResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Logs out the current user and tracks the logout activity.
     *
     * @return CommandResponse
     */
    @PostMapping("/logout")
    public ResponseEntity<CommandResponse> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        logger.info("[LOGOUT ENDPOINT] Received logout request. Authorization header: {}", authorizationHeader);
        final CommandResponse response = authService.logout();
        return ResponseEntity.ok(response);
    }
}

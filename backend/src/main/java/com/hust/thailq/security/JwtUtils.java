package com.hust.thailq.security;

import com.hust.thailq.config.MessageSourceConfig;
import com.hust.thailq.common.MessageKeys;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility class for Jwt related tasks.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final MessageSourceConfig messageConfig;

    @Value("${app.security.jwtSecret}")
    private String jwtSecret;

    @Value("${app.security.jwtExpirationMs}")
    private int jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateJwtToken(Authentication authentication) {
        final UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException e) {
            log.error(messageConfig.getMessage(MessageKeys.ERROR_JWT_INVALID_TOKEN, e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error(messageConfig.getMessage(MessageKeys.ERROR_JWT_EMPTY_CLAIMS, e.getMessage()));
        }
        return false;
    }
}
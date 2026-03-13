package com.hust.thailq.security;

import com.hust.thailq.config.MessageSourceConfig;
import com.hust.thailq.common.MessageKeys;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

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

    public String generateJwtToken(Authentication authentication) {
        final UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.error(messageConfig.getMessage(MessageKeys.ERROR_JWT_INVALID_SIGNATURE, e.getMessage()));
        } catch (MalformedJwtException e) {
            log.error(messageConfig.getMessage(MessageKeys.ERROR_JWT_INVALID_TOKEN, e.getMessage()));
        } catch (ExpiredJwtException e) {
            log.error(messageConfig.getMessage(MessageKeys.ERROR_JWT_EXPIRED, e.getMessage()));
        } catch (UnsupportedJwtException e) {
            log.error(messageConfig.getMessage(MessageKeys.ERROR_JWT_UNSUPPORTED, e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error(messageConfig.getMessage(MessageKeys.ERROR_JWT_EMPTY_CLAIMS, e.getMessage()));
        }
        return false;
    }
}

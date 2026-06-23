package com.hust.thailq.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret:YourSecretKeyForJWTTokenGenerationAndValidation}")
    private String jwtSecret;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/signup",
            "/actuator"
    );

    /**
     * RBAC Rules: Path prefix → Required roles (any match = allowed)
     */
    private static final Map<String, List<String>> RBAC_RULES = Map.of(
            "/api/v1/admin/wallets", List.of("ROLE_ADMIN", "ROLE_ACCOUNTANT", "ROLE_MANAGER", "ROLE_CASHIER"),
            "/api/v1/admin/transactions", List.of("ROLE_ADMIN", "ROLE_ACCOUNTANT"),
            "/api/v1/admin/tracking", List.of("ROLE_ADMIN"),
            "/api/v1/admin/fraud-config", List.of("ROLE_ADMIN"),
            "/api/v1/accounting", List.of("ROLE_ADMIN", "ROLE_ACCOUNTANT"),
            "/api/v1/fraud", List.of("ROLE_ADMIN", "ROLE_ACCOUNTANT"),
            "/api/v1/payments/refund-requests/", List.of("ROLE_ADMIN", "ROLE_MANAGER")
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip authentication for public endpoints
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "AUTH_001", "Missing or invalid Authorization header", path);
        }

        String token = authHeader.substring(7);

        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();

            // Extract roles from JWT claims
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);

            // RBAC enforcement: check if user has required role for the path
            if (!hasAccess(path, roles)) {
                return writeErrorResponse(exchange, HttpStatus.FORBIDDEN, "AUTH_003",
                        "Access denied. Required roles: " + getRequiredRoles(path) + ". Your roles: " + roles, path);
            }

            // Pass user info to downstream services via internal headers
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-Auth-User", username)
                    .header("X-Auth-Roles", roles != null ? String.join(",", roles) : "")
                    .header("X-Auth-Token-Valid", "true")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, "AUTH_002", "Invalid or expired token: " + e.getMessage(), path);
        }
    }

    /**
     * Check if user roles satisfy RBAC rules for the given path.
     * If no rule matches the path, access is allowed (open endpoints).
     */
    private boolean hasAccess(String path, List<String> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            // Token without roles (old token) - only allow non-restricted paths
            return RBAC_RULES.keySet().stream().noneMatch(path::startsWith);
        }

        for (Map.Entry<String, List<String>> rule : RBAC_RULES.entrySet()) {
            if (path.startsWith(rule.getKey())) {
                // Path matches a rule - check if user has any of the required roles
                return userRoles.stream().anyMatch(role -> rule.getValue().contains(role));
            }
        }

        // No rule matched - path is open to any authenticated user
        return true;
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, HttpStatus status, String errorCode, String message, String path) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"errorCode\":\"%s\",\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                Instant.now().toString(), status.value(), errorCode, status.getReasonPhrase(), message, path
        );

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private String getRequiredRoles(String path) {
        for (Map.Entry<String, List<String>> rule : RBAC_RULES.entrySet()) {
            if (path.startsWith(rule.getKey())) {
                return rule.getValue().toString();
            }
        }
        return "[]";
    }
}

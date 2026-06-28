package com.hust.thailq.common.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class AuditLogFilter extends OncePerRequestFilter {

    private final AuditLogService auditLogService;

    @Value("${spring.application.name:unknown}")
    private String serviceName;

    private static final Set<String> SKIP_PATHS = Set.of(
            "/actuator", "/swagger-ui", "/v3/api-docs"
    );

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password", "token", "secret", "credential"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Skip health checks and docs
        String path = request.getRequestURI();
        if (SKIP_PATHS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            AuditLog auditLog = AuditLog.builder()
                    .requestId(MDC.get("requestId"))
                    .serviceName(serviceName)
                    .httpMethod(request.getMethod())
                    .path(path)
                    .statusCode(wrappedResponse.getStatus())
                    .durationMs(duration)
                    .username(request.getHeader("X-Auth-User"))
                    .clientIp(getClientIp(request))
                    .callerService(request.getHeader("X-Caller-Service"))
                    .requestBody(sanitize(getBody(wrappedRequest.getContentAsByteArray())))
                    .responseBody(sanitize(getBody(wrappedResponse.getContentAsByteArray())))
                    .level(wrappedResponse.getStatus() >= 400 ? "ERROR" : "INFO")
                    .timestamp(Instant.now())
                    .build();

            auditLogService.log(auditLog);
            wrappedResponse.copyBodyToResponse();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getBody(byte[] content) {
        if (content == null || content.length == 0) return null;
        return new String(content, StandardCharsets.UTF_8);
    }

    private String sanitize(String body) {
        if (body == null) return null;
        String sanitized = body;
        for (String field : SENSITIVE_FIELDS) {
            sanitized = sanitized.replaceAll(
                    "\"" + field + "\"\\s*:\\s*\"[^\"]*\"",
                    "\"" + field + "\":\"***\"");
        }
        return sanitized;
    }
}

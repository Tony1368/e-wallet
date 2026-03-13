package com.hust.thailq.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ClientInfoService {

    private static final Pattern USER_AGENT_PATTERN = Pattern.compile(
            "([^/\\s]+)/([^\\s]+)\\s*\\(([^)]+)\\)\\s*([^\\s]*)\\s*([^\\s]*)");

    public String getClientIpAddress() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) return "unknown";

            // Log all headers for debugging
            java.util.Enumeration<String> headerNames = request.getHeaderNames();
            StringBuilder headersLog = new StringBuilder("Request headers: ");
            while (headerNames.hasMoreElements()) {
                String header = headerNames.nextElement();
                headersLog.append(header).append("=").append(request.getHeader(header)).append(", ");
            }
            log.info(headersLog.toString());

            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
                String ip = xForwardedFor.split(",")[0].trim();
                log.info("Extracted client IP from X-Forwarded-For: {}", ip);
                return ip;
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
                log.info("Extracted client IP from X-Real-IP: {}", xRealIp);
                return xRealIp;
            }

            String remoteAddr = request.getRemoteAddr();
            log.info("Extracted client IP from getRemoteAddr: {}", remoteAddr);
            return remoteAddr;
        } catch (Exception e) {
            log.warn("Error getting client IP address: {}", e.getMessage());
            return "unknown";
        }
    }

    public String getUserAgent() {
        try {
            HttpServletRequest request = getCurrentRequest();
            return request != null ? request.getHeader("User-Agent") : "unknown";
        } catch (Exception e) {
            log.warn("Error getting user agent: {}", e.getMessage());
            return "unknown";
        }
    }

    public Map<String, String> parseUserAgent(String userAgent) {
        Map<String, String> result = new HashMap<>();
        
        if (userAgent == null || userAgent.isEmpty()) {
            result.put("browser", "unknown");
            result.put("operatingSystem", "unknown");
            result.put("deviceType", "unknown");
            return result;
        }

        // Parse browser and OS
        Matcher matcher = USER_AGENT_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            result.put("browser", matcher.group(1));
            result.put("operatingSystem", matcher.group(3));
        } else {
            result.put("browser", "unknown");
            result.put("operatingSystem", "unknown");
        }

        // Determine device type
        String deviceType = "desktop";
        String lowerUserAgent = userAgent.toLowerCase();
        
        if (lowerUserAgent.contains("mobile") || lowerUserAgent.contains("android") || lowerUserAgent.contains("iphone")) {
            deviceType = "mobile";
        } else if (lowerUserAgent.contains("tablet") || lowerUserAgent.contains("ipad")) {
            deviceType = "tablet";
        }
        
        result.put("deviceType", deviceType);
        return result;
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.warn("Error getting current request: {}", e.getMessage());
            return null;
        }
    }
} 
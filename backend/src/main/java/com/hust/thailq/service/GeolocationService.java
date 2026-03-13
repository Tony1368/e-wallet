package com.hust.thailq.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for geolocation functionality.
 * This is a basic implementation that can be extended to integrate with real geolocation APIs.
 */
@Slf4j
@Service
public class GeolocationService {

    /**
     * Get geolocation information from IP address.
     * This is a placeholder implementation that returns default values.
     * In a real implementation, you would integrate with services like:
     * - MaxMind GeoIP2
     * - IP2Location
     * - ipapi.com
     * - ipstack.com
     */
    public Map<String, String> getLocationFromIp(String ipAddress) {
        Map<String, String> location = new HashMap<>();
        log.info("[Geo] Getting location for IP: {}", ipAddress);
        
        // Skip localhost and private IP addresses
        if (ipAddress == null || ipAddress.isEmpty() || 
            ipAddress.equals("unknown") || 
            ipAddress.startsWith("127.") || 
            ipAddress.startsWith("192.168.") || 
            ipAddress.startsWith("10.") || 
            ipAddress.startsWith("172.")) {
            log.info("[Geo] IP is local/private, returning default location.");
            location.put("country", "Unknown");
            location.put("city", "Unknown");
            location.put("region", "Unknown");
            location.put("latitude", "0");
            location.put("longitude", "0");
            location.put("timezone", "UTC");
            return location;
        }

        // Use ip-api.com for real geolocation
        try {
            String url = "http://ip-api.com/json/" + ipAddress;
            log.info("[Geo] Calling geolocation API: {}", url);
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            log.info("[Geo] API response: {}", response);
            
            if (response != null && "success".equals(response.get("status"))) {
                location.put("country", (String) response.get("country"));
                location.put("city", (String) response.get("city"));
                location.put("region", (String) response.get("regionName"));
                location.put("latitude", String.valueOf(response.get("lat")));
                location.put("longitude", String.valueOf(response.get("lon")));
                location.put("timezone", (String) response.get("timezone"));
            } else {
                log.warn("[Geo] API did not return success or response is null. Returning default location.");
                setDefaultLocation(location);
            }
        } catch (Exception e) {
            log.warn("[Geo] Error getting geolocation for IP {}: {}", ipAddress, e.getMessage());
            setDefaultLocation(location);
        }
        return location;
    }

    private void setDefaultLocation(Map<String, String> location) {
        location.put("country", "Unknown");
        location.put("city", "Unknown");
        location.put("region", "Unknown");
        location.put("latitude", "0");
        location.put("longitude", "0");
        location.put("timezone", "UTC");
    }
} 
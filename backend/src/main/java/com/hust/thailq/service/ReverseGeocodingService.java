package com.hust.thailq.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ReverseGeocodingService {
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/reverse";
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, String> reverseGeocode(double latitude, double longitude) {
        Map<String, String> result = new HashMap<>();
        try {
            String url = UriComponentsBuilder.fromHttpUrl(NOMINATIM_URL)
                    .queryParam("lat", latitude)
                    .queryParam("lon", longitude)
                    .queryParam("format", "json")
                    .queryParam("addressdetails", 1)
                    .toUriString();

            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("address")) {
                Map address = (Map) response.get("address");
                result.put("city", (String) address.getOrDefault("city", address.getOrDefault("town", address.getOrDefault("village", ""))));
                result.put("region", (String) address.getOrDefault("state", ""));
                result.put("country", (String) address.getOrDefault("country", ""));
                // Timezone is not provided by Nominatim; leave blank or use a separate API if needed
                result.put("timezone", "");
            }
        } catch (Exception e) {
            log.warn("Reverse geocoding failed for lat={}, lon={}: {}", latitude, longitude, e.getMessage());
            result.put("city", "");
            result.put("region", "");
            result.put("country", "");
            result.put("timezone", "");
        }
        return result;
    }
} 
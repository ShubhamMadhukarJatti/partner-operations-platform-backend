package com.sharkdom.startupAutomation.service;

import com.sharkdom.startupAutomation.dto.SubsectorResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SubsectorService {

    private static final String SUBSECTOR_URL =
            "https://sharkdomfilters-b6haf7g5bmeeendy.centralindia-01.azurewebsites.net/query_subsector";

    private final RestTemplate restTemplate;

    public SubsectorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public SubsectorResponse getSubsectors(String input) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestJson = String.format("{\"input\": \"%s\"}", input);

            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            ResponseEntity<SubsectorResponse> response = restTemplate.exchange(
                    SUBSECTOR_URL,
                    HttpMethod.POST,
                    entity,
                    SubsectorResponse.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch subsector details: " + e.getMessage(), e);
        }
    }
}

package com.sharkdom.startupAutomation.service;

import com.sharkdom.startupAutomation.dto.StartupDetailsResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AutomationService {

    private static final String AUTOMATION_URL = "https://sharkdom-automation.azurewebsites.net/automation/";
    private final RestTemplate restTemplate;

    public AutomationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public StartupDetailsResponse getStartupDetails(String site) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestJson = String.format("{\"site\": \"%s\"}", site);

            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            ResponseEntity<StartupDetailsResponse> response = restTemplate.exchange(
                    AUTOMATION_URL,
                    HttpMethod.POST,
                    entity,
                    StartupDetailsResponse.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch startup details: " + e.getMessage(), e);
        }
    }
}

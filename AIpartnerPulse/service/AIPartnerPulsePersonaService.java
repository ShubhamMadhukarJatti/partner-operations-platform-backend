package com.sharkdom.AIpartnerPulse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.AIpartnerPulse.dto.PartnerPersonaFilteredResponse;
import com.sharkdom.AIpartnerPulse.dto.PartnerPersonaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIPartnerPulsePersonaService {


    @Value("${partner.persona.api.url}")
    private String partnerPersonaApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PartnerPersonaResponse fetchPartnerPersonaData(String... urls) {
        try {
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("urls", urls);

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            log.info("Sending request to PartnerPersona API [{}] with URLs: {}", partnerPersonaApiUrl, String.join(", ", urls));

            ResponseEntity<PartnerPersonaResponse> response = restTemplate.exchange(
                    partnerPersonaApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    PartnerPersonaResponse.class
            );

            log.info("Received PartnerPersona response with status: {}", response.getStatusCode());
            return response.getBody();

        } catch (Exception e) {
            log.error("Error fetching PartnerPersona data from {}", partnerPersonaApiUrl, e);
            throw new RuntimeException("Failed to fetch PartnerPersona data", e);
        }
    }

    public PartnerPersonaFilteredResponse fetchExternalPartnerPersonaData(String... urls) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("urls", urls);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            log.info("Sending request to PartnerPersona API [{}] with URLs: {}", partnerPersonaApiUrl, String.join(", ", urls));

            ResponseEntity<String> response = restTemplate.exchange(
                    partnerPersonaApiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode results = root.path("results").get(0); // Assuming single URL

            // Extract ranked_threshold keys
            JsonNode rankedThresholds = results.path("division_1").path("ranked_thresholds");
            Iterator<String> fieldNames = rankedThresholds.fieldNames();
            List<String> rankedThresholdKeys = new ArrayList<>();
            while (fieldNames.hasNext()) {
                rankedThresholdKeys.add(fieldNames.next());
            }

            // Extract predicted_subsectors
            List<String> predictedSubsectors = new ArrayList<>();
            JsonNode subsectors = results.path("division_3").path("predicted_subsectors");
            if (subsectors.isArray()) {
                subsectors.forEach(node -> predictedSubsectors.add(node.asText()));
            }

            // Convert lists to comma-separated strings
            String rankedThresholdKeysStr = String.join(", ", rankedThresholdKeys);
            String predictedSubsectorsStr = String.join(", ", predictedSubsectors);

            PartnerPersonaFilteredResponse filtered = new PartnerPersonaFilteredResponse();
            filtered.setRankedThresholdKeys(rankedThresholdKeysStr);
            filtered.setPredictedSubsectors(predictedSubsectorsStr);

            log.info("Filtered Response: {}", filtered);
            return filtered;

        } catch (Exception e) {
            log.error("Error fetching PartnerPersona data from {}", partnerPersonaApiUrl, e);
            throw new RuntimeException("Failed to fetch PartnerPersona data", e);
        }
    }
}

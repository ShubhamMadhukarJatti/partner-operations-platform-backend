package com.sharkdom.agenticai.service.impl;

import com.sharkdom.agenticai.confignew.DweepAgentConfig;
import com.sharkdom.agenticai.model.GenerateLinkedinNoteRequest;
import com.sharkdom.agenticai.model.SendConnectionRequestRequest;
import com.sharkdom.agenticai.service.LinkedInOutreachService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LinkedInOutreachServiceImpl implements LinkedInOutreachService {

    private final DweepAgentConfig config;
    private final RestTemplate restTemplate;

    @Override
    public Map<String, Object> getAccountStatus(String accountId) {
        String url = config.getBaseUrl() 
            + "/api/v1/linkedin/outreach/accounts/" + accountId + "/status";
        return restTemplate.getForObject(url, Map.class);
    }

    @Override
    public Map<String, Object> listConnections(String accountId) {
        String url = config.getBaseUrl() 
            + "/api/v1/linkedin/outreach/accounts/" + accountId + "/connections";
        return restTemplate.getForObject(url, Map.class);
    }

    @Override
    public Map<String, Object> generateNote(GenerateLinkedinNoteRequest request) {
        String url = config.getBaseUrl() + "/api/v1/linkedin/outreach/generate-note";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GenerateLinkedinNoteRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            url, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }

    @Override
    public Map<String, Object> sendConnectionRequest(SendConnectionRequestRequest request) {
        String url = config.getBaseUrl() + "/api/v1/linkedin/outreach/send-connection-request";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SendConnectionRequestRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            url, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }
}
package com.sharkdom.agenticai.service.impl;

import com.sharkdom.agenticai.confignew.DweepAgentConfig;
import com.sharkdom.agenticai.model.LinkedInCookieConnectRequest;
import com.sharkdom.agenticai.service.LinkedinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LinkedInServiceImpl implements LinkedinService {

    private final DweepAgentConfig config;
    private final RestTemplate restTemplate;

    @Override
    public Map<String, Object> listAccounts() {
        String url = config.getBaseUrl() + "/api/v1/auth/accounts";
        return restTemplate.getForObject(url, Map.class);
    }

    @Override
    public Map<String, Object> authenticate() {
        String url = config.getBaseUrl() + "/api/v1/auth/authenticate";
        return restTemplate.getForObject(url, Map.class);
    }

    @Override
    public Map<String, Object> reconnect() {
        String url = config.getBaseUrl() + "/api/v1/auth/accounts/reconnect";
        return restTemplate.getForObject(url, Map.class);
    }

    @Override
    public Map<String, Object> authenticateCookie(LinkedInCookieConnectRequest request) {
        String url = config.getBaseUrl() + "/api/v1/auth/authenticate/cookie";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LinkedInCookieConnectRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            url, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }

    @Override
    public Map<String, Object> deleteAccount(String accountId) {
        String url = config.getBaseUrl() + "/api/v1/auth/accounts/delete?account_id=" + accountId;
        ResponseEntity<Map> response = restTemplate.exchange(
            url, HttpMethod.DELETE, null, Map.class);
        return response.getBody();
    }
}
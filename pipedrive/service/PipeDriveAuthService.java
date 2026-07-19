package com.sharkdom.pipedrive.service;

import com.sharkdom.pipedrive.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class PipeDriveAuthService {

    @Value("${pipedrive.client-id}")
    private String clientId;

    @Value("${pipedrive.client-secret}")
    private String clientSecret;

    private final String TOKEN_URL = "https://oauth.pipedrive.com/oauth/token";

    public TokenResponse refreshAccessToken(String refreshToken) {
        RestTemplate restTemplate = new RestTemplate();

        // Request body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> res = response.getBody();
            String accessToken = (String) res.get("access_token");
            String newRefreshToken = (String) res.get("refresh_token");
            return new TokenResponse(accessToken, newRefreshToken);
        } else {
            throw new RuntimeException("Failed to refresh token. Status: " + response.getStatusCode());
        }
    }
}

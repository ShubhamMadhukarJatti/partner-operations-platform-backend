package com.sharkdom.service.partenerDeals.hubspot;

import com.sharkdom.service.partenerDeals.hubspot.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class HubSpotAuthService {

    @Value("${hubspot.id}")
    private String clientId;

    @Value("${hubspot.secret}")
    private String clientSecret;

    private static final String TOKEN_URL = "https://api.hubapi.com/oauth/v1/token";

    public TokenResponse getAccessTokenUsingRefreshToken(String refreshToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String accessToken = response.getBody().get("access_token").toString();
            String newRefreshToken = response.getBody().get("refresh_token").toString(); // New refresh token
            return new TokenResponse(accessToken, newRefreshToken);
        } else {
            throw new RuntimeException("Failed to fetch access token from HubSpot");
        }
    }


}

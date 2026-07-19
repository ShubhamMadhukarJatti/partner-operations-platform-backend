package com.sharkdom.salesforce.service;

import com.sharkdom.salesforce.dto.SalesforceTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class SalesforceAuthService {

    @Value("${salesforce.oauth.clientId}")
    private String clientId;

    @Value("${salesforce.oauth.clientSecret}")
    private String clientSecret;

    @Value("${salesforce.oauth.clientIdEps}")
    private String clientIdEps;

    @Value("${salesforce.oauth.clientSecretEps}")
    private String clientSecretEps;

    private static final String TOKEN_URL =
            "https://login.salesforce.com/services/oauth2/token";

    private final RestTemplate restTemplate;

    /**
     * Refresh token using default connected app.
     */
    public SalesforceTokenResponse refreshAccessToken(String refreshToken) {
        return refreshToken(refreshToken, clientId, clientSecret);
    }

    /**
     * Refresh token using EPS connected app.
     */
    public SalesforceTokenResponse refreshAccessTokenForEPS(String refreshToken) {
        return refreshToken(refreshToken, clientIdEps, clientSecretEps);
    }

    /**
     * Returns only the access token.
     */
    public String getAccessToken(String refreshToken) {
        return refreshAccessToken(refreshToken).accessToken();
    }

    /**
     * Returns only the instance URL.
     */
    public String getInstanceUrl(String refreshToken) {
        return refreshAccessToken(refreshToken).instanceUrl();
    }

    /**
     * Common implementation.
     */
    private SalesforceTokenResponse refreshToken(
            String refreshToken,
            String clientId,
            String clientSecret) {

        MultiValueMap<String, String> formData =
                new LinkedMultiValueMap<>();

        formData.add("grant_type", "refresh_token");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(formData, headers);

        ResponseEntity<SalesforceTokenResponse> response =
                restTemplate.exchange(
                        TOKEN_URL,
                        HttpMethod.POST,
                        request,
                        SalesforceTokenResponse.class
                );

        if (!response.getStatusCode().is2xxSuccessful()
                || response.getBody() == null) {

            throw new IllegalStateException(
                    "Unable to refresh Salesforce access token.");
        }

        return response.getBody();
    }
}

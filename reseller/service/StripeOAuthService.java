package com.sharkdom.reseller.service;

import com.sharkdom.reseller.dto.StripeOAuthTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class StripeOAuthService {

    private final RestTemplate restTemplate;

    @Value("${stripe.secret-key}")
    private String secretKey;

    private static final String STRIPE_OAUTH_URL =
            "https://connect.stripe.com/oauth/token";

    public StripeOAuthTokenResponse exchangeAuthCodeForToken(String authorizationCode) {

        // EXACT MATCH to curl → base64(sk_test_xxx:)
        String auth = secretKey + ":";
        String encodedAuth = Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", authorizationCode);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<StripeOAuthTokenResponse> response =
                restTemplate.exchange(
                        STRIPE_OAUTH_URL,
                        HttpMethod.POST,
                        request,
                        StripeOAuthTokenResponse.class
                );

        return response.getBody();
    }
}


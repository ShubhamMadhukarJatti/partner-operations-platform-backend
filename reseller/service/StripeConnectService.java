package com.sharkdom.reseller.service;

import com.sharkdom.reseller.dto.StripeTokenResponse;
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
public class StripeConnectService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    private static final String STRIPE_TOKEN_URL =
            "https://connect.stripe.com/oauth/token";


    public StripeTokenResponse refreshAccessToken(String refreshToken) {

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String auth = stripeSecretKey + ":";
            String encodedAuth = Base64.getEncoder()
                    .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            headers.set("Authorization", "Basic " + encodedAuth);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<StripeTokenResponse> response =
                    restTemplate.exchange(
                            STRIPE_TOKEN_URL,
                            HttpMethod.POST,
                            request,
                            StripeTokenResponse.class
                    );

            return response.getBody();

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Failed to refresh Stripe access token", ex
            );
        }
    }
}
package com.sharkdom.reseller.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class StripePaymentIntentService {

    private final RestTemplate restTemplate;

    @Value("${stripe.secret-key}")
    private String secretKey;

    private static final String STRIPE_PAYMENT_INTENT_URL =
            "https://api.stripe.com/v1/payment_intents";

    public String getPaymentIntents(String connectedAccountId) {

        // base64(sk_test_xxx:)
        String auth = secretKey + ":";
        String encodedAuth = Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
        headers.set("Stripe-Account", connectedAccountId);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        STRIPE_PAYMENT_INTENT_URL,
                        HttpMethod.GET,
                        request,
                        String.class
                );

        return response.getBody();
    }
}


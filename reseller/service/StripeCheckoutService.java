package com.sharkdom.reseller.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.reseller.dto.CreateCheckoutSessionRequest;
import com.sharkdom.reseller.dto.StripeCheckoutSessionResponse;
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
public class StripeCheckoutService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${stripe.secret-key}")
    private String secretKey;

    private static final String STRIPE_CHECKOUT_URL =
            "https://api.stripe.com/v1/checkout/sessions";

    public String createCheckoutSession(CreateCheckoutSessionRequest requestDto) {

        // Base64(sk_test_xxx:)
        String auth = secretKey + ":";
        String encodedAuth = Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
        headers.set("Stripe-Account", requestDto.getConnectedAccountId());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("mode", "payment");
        body.add("customer_email", requestDto.getCustomerEmail());
        body.add("billing_address_collection", "required");

        body.add("shipping_address_collection[allowed_countries][]", "IN");

        body.add("line_items[0][price_data][currency]", requestDto.getCurrency());
        body.add("line_items[0][price_data][product_data][name]", requestDto.getProductName());
        body.add("line_items[0][price_data][unit_amount]",
                String.valueOf(requestDto.getUnitAmount()));
        body.add("line_items[0][quantity]",
                String.valueOf(requestDto.getQuantity()));

        body.add("success_url", requestDto.getSuccessUrl());
        body.add("cancel_url", requestDto.getCancelUrl());

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        STRIPE_CHECKOUT_URL,
                        HttpMethod.POST,
                        request,
                        String.class
                );

        return response.getBody(); // contains session.id + session.url
    }

    public String getCheckoutSession(String sessionId, String connectedAccountId) {

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
                        STRIPE_CHECKOUT_URL+"/" + sessionId,
                        HttpMethod.GET,
                        request,
                        String.class
                );

        return response.getBody();
    }

    public StripeCheckoutSessionResponse createCheckoutSessionURL(CreateCheckoutSessionRequest requestDto) {

        String auth = secretKey + ":";
        String encodedAuth = Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
        headers.set("Stripe-Account", requestDto.getConnectedAccountId());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("mode", "payment");
        body.add("customer_email", requestDto.getCustomerEmail());
        body.add("billing_address_collection", "required");

        body.add("shipping_address_collection[allowed_countries][]", "IN");

        body.add("line_items[0][price_data][currency]", requestDto.getCurrency());
        body.add("line_items[0][price_data][product_data][name]", requestDto.getProductName());
        body.add("line_items[0][price_data][unit_amount]",
                String.valueOf(requestDto.getUnitAmount()));
        body.add("line_items[0][quantity]",
                String.valueOf(requestDto.getQuantity()));

        body.add("success_url", requestDto.getSuccessUrl());
        body.add("cancel_url", requestDto.getCancelUrl());

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        STRIPE_CHECKOUT_URL,
                        HttpMethod.POST,
                        request,
                        String.class
                );

        try {

            JsonNode json = objectMapper.readTree(response.getBody());

            return new StripeCheckoutSessionResponse(
                    json.get("id").asText(),
                    json.get("url").asText()
            );

        } catch (Exception e) {
            throw new RuntimeException("Stripe response parsing failed", e);
        }
    }
}

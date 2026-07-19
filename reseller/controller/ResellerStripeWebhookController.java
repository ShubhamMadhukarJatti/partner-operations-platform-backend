package com.sharkdom.reseller.controller;

import com.sharkdom.reseller.service.ResellerStripeWebhookService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Stripe Webhook Controller for Reseller Payments
 *
 * Handles Stripe webhook callbacks such as:
 * - Payment success
 * - Payment failure
 * - Checkout session events
 *
 * Note: This API is hidden from Swagger UI and publicly accessible.
 */
@Slf4j
@RestController
@RequestMapping("/payment/reseller")
@RequiredArgsConstructor
public class ResellerStripeWebhookController {

    private final ResellerStripeWebhookService stripeWebhookService;

    /** Handle Stripe webhook callback */
    @Hidden
    @PermitAll
    @Operation(summary="Stripe Webhook Callback",description="Handles Stripe webhook events",hidden=true)
    @PostMapping("/stripe/callback")
    public ResponseEntity<String> handleWebhook(
            @Parameter(description="Stripe payload") @RequestBody String payload,
            @Parameter(description="Stripe signature header") @RequestHeader("Stripe-Signature") String sigHeader){

        log.info("Stripe webhook received");
        return stripeWebhookService.handleStripeWebhook(payload,sigHeader);
    }
}
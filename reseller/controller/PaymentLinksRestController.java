package com.sharkdom.reseller.controller;

import com.sharkdom.reseller.dto.*;
import com.sharkdom.reseller.service.*;
import com.sharkdom.util.SharkdomApiResponse;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Stripe Payment Controller
 *
 * Handles:
 * - OAuth token exchange
 * - Checkout session creation
 * - Payment intent retrieval
 * - Access token refresh
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Tag(name="Stripe Payment APIs",description="Stripe OAuth, Checkout & Payment APIs")
public class PaymentLinksRestController {

    private final StripeOAuthService stripeOAuthService;
    private final StripeCheckoutService stripeCheckoutService;
    private final StripePaymentIntentService stripePaymentIntentService;
    private final StripeConnectService stripeConnectService;

    /** Exchange Stripe auth code for access token */
    @Operation(summary="Exchange Stripe OAuth Code",description="Convert auth code to access & refresh token")
    @ApiResponse(responseCode="200",description="Token exchanged successfully")
    @PostMapping("/stripe/oauth/token")
    public StripeOAuthTokenResponse exchangeAuthCode(
            @Parameter(description="Stripe auth code",required=true) @RequestParam String code){
        log.info("Stripe OAuth token exchange");
        return stripeOAuthService.exchangeAuthCodeForToken(code);
    }

    /** Create Stripe checkout session */
    @Operation(summary="Create Checkout Session")
    @ApiResponse(responseCode="200",description="Checkout session created")
    @PostMapping("/stripe/checkout/session")
    public ResponseEntity<String> createCheckoutSession(@RequestBody CreateCheckoutSessionRequest req){
        log.info("Create checkout session accountId={} amount={}",req.getConnectedAccountId(),req.getUnitAmount());
        return ResponseEntity.ok(stripeCheckoutService.createCheckoutSession(req));
    }

    /** Get checkout session details */
    @Operation(summary="Get Checkout Session")
    @ApiResponse(responseCode="200",description="Session fetched successfully")
    @GetMapping("/stripe/checkout/session/{sessionId}")
    public ResponseEntity<String> getCheckoutSession(
            @Parameter(description="Session ID") @PathVariable String sessionId,
            @Parameter(description="Connected Account ID") @RequestParam String connectedAccountId){
        log.info("Fetch session sessionId={} accountId={}",sessionId,connectedAccountId);
        return ResponseEntity.ok(stripeCheckoutService.getCheckoutSession(sessionId,connectedAccountId));
    }

    /** Get payment intents */
    @Operation(summary="Get Payment Intents")
    @ApiResponse(responseCode="200",description="Payment intents fetched")
    @GetMapping("/stripe/payment-intents")
    public ResponseEntity<String> getPaymentIntents(
            @Parameter(description="Connected Account ID") @RequestParam String connectedAccountId){
        log.info("Fetch payment intents accountId={}",connectedAccountId);
        return ResponseEntity.ok(stripePaymentIntentService.getPaymentIntents(connectedAccountId));
    }

    /** Refresh Stripe access token */
    @Operation(summary="Refresh Access Token",description="Generate new access token using refresh token")
    @ApiResponse(responseCode="200",description="Token refreshed successfully")
    @PostMapping("/refresh-token")
    public ResponseEntity<SharkdomApiResponse<StripeTokenResponse>> refreshToken(
            @Parameter(description="Refresh Token",required=true) @RequestParam String refreshToken){
        var res=stripeConnectService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Access token refreshed successfully",res));
    }
}
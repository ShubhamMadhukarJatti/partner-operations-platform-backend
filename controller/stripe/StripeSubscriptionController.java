package com.sharkdom.controller.stripe;

import com.sharkdom.constants.stripe.StripePlanType;
import com.sharkdom.model.stripe.CreateSubscriptionRequest;
import com.sharkdom.model.stripe.StripeSubscriptionDataDto;
import com.sharkdom.model.stripe.UpgradeResponseDTO;
import com.sharkdom.service.stripe.StripeSubscriptionService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sharkdom-stripe/v1")
@RequiredArgsConstructor
public class StripeSubscriptionController {

    private final StripeSubscriptionService stripeSubscriptionService;

    @Operation(summary = "get subscription ")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "get subscription ", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StripeSubscriptionDataDto.class)) }),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content) })
    @GetMapping("/subscription")
    public ResponseEntity<StripeSubscriptionDataDto> getSubscriptionBySubscriptionId(
            @RequestParam(value = "subscriptionId") String subscriptionId) throws StripeException {
        return ResponseEntity.ok().body(stripeSubscriptionService.getSubscriptionBySubscriptionId(subscriptionId));
    }

    @Operation(summary = "get subscription by organizationId ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "get subscription by organizationId", content = {
                    @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = StripeSubscriptionDataDto.class)) }),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content) })
    @GetMapping("/subscription/{orgId}")
    public ResponseEntity<List<StripeSubscriptionDataDto>> getSubscriptionsByOrganizationId(@PathVariable Long orgId) {
        List<StripeSubscriptionDataDto> subscriptions = stripeSubscriptionService
                .getSubscriptionsByOrganizationId(orgId);
        return ResponseEntity.ok(subscriptions);
    }

    @Operation(summary = "cancel subscription ")
    @ApiResponses(value = { @ApiResponse(responseCode = "202", description = "cancel subscription ", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StripeSubscriptionDataDto.class)) }),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content) })
    @PutMapping("/subscription/cancel")
    public ResponseEntity<StripeSubscriptionDataDto> cancelSubscription(
            @RequestParam(value = "subscriptionId") String subscriptionId,
            @RequestParam(defaultValue = "false") boolean requestRefund) throws StripeException {
        StripeSubscriptionDataDto canceledSubscription = stripeSubscriptionService.cancelSubscription(subscriptionId,
                requestRefund);
        return ResponseEntity.accepted().body(canceledSubscription);
    }

    @Operation(summary = "update subscription ")
    @ApiResponses(value = { @ApiResponse(responseCode = "202", description = "update subscription ", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = UpgradeResponseDTO.class)) }),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content) })
    @PutMapping("/subscription/upgrade")
    public ResponseEntity<UpgradeResponseDTO> upgradeDowngradeSubscription(
            @RequestParam(value = "subscriptionId") String subscriptionId,
            @RequestParam(value = "planType") StripePlanType planType,
            @RequestParam(value = "successUrl") String successUrl, @RequestParam(value = "cancelUrl") String cancelUrl)
            throws StripeException {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(stripeSubscriptionService.upgradeSubscription(subscriptionId, planType, successUrl, cancelUrl));
    }

    @Operation(summary = "upgrade subscription seat ")
    @ApiResponses(value = { @ApiResponse(responseCode = "202", description = "upgrade subscription seat", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = UpgradeResponseDTO.class)) }),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content) })
    @PutMapping("/subscription/upgrade-seat")
    public ResponseEntity<UpgradeResponseDTO> upgradeSeat(@RequestParam(value = "subscriptionId") String subscriptionId,
                                                          @RequestParam(value = "planType") StripePlanType planType,
                                                          @RequestParam(value = "successUrl") String successUrl, @RequestParam(value = "cancelUrl") String cancelUrl)
            throws StripeException {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(stripeSubscriptionService.upgradeSeat(subscriptionId, planType, successUrl, cancelUrl));
    }

//    @Operation(summary = "downgrade subscription ")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "202", description = "downgrade subscription ", content = {
//                    @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)) }),
//            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content) })
//    @PutMapping("/subscription/downgrade")
//    public ResponseEntity<Map<String, Object>> downgradeSubscription(
//            @RequestParam(value = "subscriptionId") String subscriptionId,
//            @RequestParam(value = "planType") StripePlanType planType,
//            @RequestParam(value = "successUrl") String successUrl, @RequestParam(value = "cancelUrl") String cancelUrl)
//            throws StripeException {
//        return ResponseEntity.status(HttpStatus.ACCEPTED)
//                .body(stripeSubscriptionService.downgradeSubscription(subscriptionId, planType, successUrl, cancelUrl));
//    }

    @Operation(summary = "downgrade seat by subscription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "downgrade seat by subscription ", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)) }),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content) })
    @PutMapping("/subscription/downgrade-seat")
    public ResponseEntity<Map<String, Object>> downgradeSeat(
            @RequestParam(value = "subscriptionId") String subscriptionId,
            @RequestParam(value = "planType") StripePlanType planType,
            @RequestParam(value = "successUrl") String successUrl, @RequestParam(value = "cancelUrl") String cancelUrl)
            throws StripeException {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(stripeSubscriptionService.downgradeSeat(subscriptionId, planType, successUrl, cancelUrl));
    }

    @Operation(summary = "create subscription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subscription created successfully", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = StripeSubscriptionDataDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Customer or plan not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @PostMapping("/subscription/create")
    public ResponseEntity<StripeSubscriptionDataDto> createSubscription(
            @RequestBody CreateSubscriptionRequest request) throws StripeException {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(stripeSubscriptionService.createSubscription(request));
    }
}

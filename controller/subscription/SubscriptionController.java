package com.sharkdom.controller.subscription;

import com.sharkdom.constants.subscription.SubscriptionStatus;
import com.sharkdom.entity.subscription.Subscription;
import com.sharkdom.service.subscription.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController

@RequestMapping("/subscription")
@Slf4j
public class SubscriptionController {

    @Autowired
    SubscriptionService subscriptionService;

    @Operation(summary = "Get latest subscription of a organization by organizationId ")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found entries.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = Subscription.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/{organizationId}")
    public ResponseEntity<Subscription> findOrganizationId(
            @PathVariable(name = "organizationId") Long organizationId) {
        return ResponseEntity.ok(subscriptionService.findByOrganizationId(organizationId));
    }

    @Operation(summary = "Get all subscriptions of a user by UserId and subscription status")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found entries.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = Subscription.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/byUserIdAndStatus")
    public ResponseEntity<List<Subscription>> findAllByUserIdAndStatus(
            @RequestParam(name = "userId") String userId,
            @RequestParam(value = "statusArray[]") SubscriptionStatus[] statusArray) {
        return ResponseEntity.ok(subscriptionService.findAllByUserIdAndStatus(userId, statusArray));
    }

    @Operation(summary = "Get all subscriptions of an organization by organization_id and subscription status")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Found entries.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = Subscription.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/byOrganizationIdAndStatus")
    public ResponseEntity<List<Subscription>> findAllByOrganizationIdAndStatus(
            @RequestParam(name = "organizationId") long organizationId,
            @RequestParam(value = "statusArray[]") SubscriptionStatus[] statusArray) {
        return ResponseEntity.ok(subscriptionService.findAllByOrganizationIdAndStatus(organizationId, statusArray));
    }

//    @Operation(summary = "Create or update a subscription by sending subscription json")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Subscription created/updated successfully.", content = {
//                    @Content(mediaType = "application/json", schema = @Schema(implementation = Subscription.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
//    @PostMapping("")
//    public ResponseEntity<Subscription> createOrUpdate(@RequestBody Subscription subscription) {
//        return ResponseEntity.ok(subscriptionService.createOrUpdate(subscription));
//    }

    @Operation(summary = "Cancel a subscription, if user cancels trial plan then the next month plan(if any) will be also cancelled together.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription(s) cancelled successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Subscription.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/cancel")
    public ResponseEntity<List<Subscription>> cancelSubscription(@RequestParam(name = "cancellationReason") String cancellationReason,
                                                                 @RequestParam(name = "subscriptionId") long id) {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(id, cancellationReason));
    }

    @Operation(summary = "Get trial subscription details")
    @GetMapping("/trial")
    public ResponseEntity<Optional<Subscription>> getTrialSubscription(
            @RequestParam(name = "organizationId") long organizationId) {
        return ResponseEntity.ok(subscriptionService.trialSubscription(organizationId));
    }


}

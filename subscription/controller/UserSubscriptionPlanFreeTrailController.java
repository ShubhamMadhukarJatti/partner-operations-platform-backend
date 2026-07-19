package com.sharkdom.subscription.controller;

import com.sharkdom.subscription.model.UserSubscriptionPlanFreeTrailRequest;
import com.sharkdom.subscription.model.UserSubscriptionPlanFreeTrailResponse;
import com.sharkdom.subscription.service.ModuleSubscriptionPlanService;
import com.sharkdom.util.SharkdomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/free-trial-subscription-plans")
@RequiredArgsConstructor
public class UserSubscriptionPlanFreeTrailController {

    private final ModuleSubscriptionPlanService userSubscriptionPlanFreeTrailService;

    /**
     * Create Free Trial Subscription Plan
     */
    @Operation(summary = "Create Free Trial Subscription Plan Profile")
    @PostMapping("/email/{email}")
    public SharkdomApiResponse<UserSubscriptionPlanFreeTrailResponse> createFreeTrialSubscriptionPlan(
            @Valid @RequestBody UserSubscriptionPlanFreeTrailRequest request,
            @PathVariable("email") String userEmail) {

        log.info("[CREATE] Free Trial Subscription Plan | userEmail={} | request={}",
                userEmail, request);

        SharkdomApiResponse<UserSubscriptionPlanFreeTrailResponse> response =
                userSubscriptionPlanFreeTrailService
                        .upsertFreeTrialSubscriptionPlan(request, userEmail);

        log.info("[CREATE-SUCCESS] Free Trial Subscription Plan | userEmail={}", userEmail);

        return response;
    }

    /**
     * Get Free Trial Subscription Plan by Email
     */
    @Operation(summary = "Get Free Trial Subscription Plan By Email")
    @GetMapping("/email/{email}")
    public SharkdomApiResponse<UserSubscriptionPlanFreeTrailResponse> getFreeTrialSubscriptionPlan(
            @PathVariable("email") String userEmail) {

        log.info("[FETCH] Free Trial Subscription Plan | userEmail={}", userEmail);

        SharkdomApiResponse<UserSubscriptionPlanFreeTrailResponse> response =
                userSubscriptionPlanFreeTrailService
                        .getFreeTrialSubscriptionPlanByEmail(userEmail);

        log.info("[FETCH-SUCCESS] Free Trial Subscription Plan | userEmail={}", userEmail);

        return response;
    }

    /**
     * Update Free Trial Subscription Plan by Email
     */
    @Operation(summary = "Update Free Trial Subscription Plan By Email")
    @PutMapping("/email/{email}")
    public SharkdomApiResponse<UserSubscriptionPlanFreeTrailResponse> updateFreeTrialSubscriptionPlan(
            @Valid @RequestBody UserSubscriptionPlanFreeTrailRequest request,
            @PathVariable("email") String userEmail) {

        log.info("[UPDATE] Free Trial Subscription Plan | userEmail={} | request={}",
                userEmail, request);

        SharkdomApiResponse<UserSubscriptionPlanFreeTrailResponse> response =
                userSubscriptionPlanFreeTrailService
                        .updateFreeTrialSubscriptionPlanByEmail(request, userEmail);

        log.info("[UPDATE-SUCCESS] Free Trial Subscription Plan | userEmail={}", userEmail);

        return response;
    }
}
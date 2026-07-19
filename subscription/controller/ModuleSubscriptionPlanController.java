package com.sharkdom.subscription.controller;

import com.sharkdom.subscription.model.ModuleSubscriptionPlanRequest;
import com.sharkdom.subscription.model.ModuleSubscriptionPlanResponse;
import com.sharkdom.subscription.service.ModuleSubscriptionPlanService;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/module-subscription-plans")
@RequiredArgsConstructor
@Slf4j
public class ModuleSubscriptionPlanController {

    private final ModuleSubscriptionPlanService service;

    /**
     * Create Module Subscription Plan
     */
    @Operation(summary = "Create Module Subscription Plan Profile")
    @PostMapping
    public SharkdomApiResponse<ModuleSubscriptionPlanResponse> createSubscriptionPlan(
            @Valid @RequestBody ModuleSubscriptionPlanRequest request) {

        Long orgId = Util.getOrgIdFromToken();
        log.info("[CREATE] Module Subscription Plan | orgId={} | request={}", orgId, request);

        SharkdomApiResponse<ModuleSubscriptionPlanResponse> response =
                service.upsertSubscriptionPlan(request, orgId);

        log.info("[CREATE-SUCCESS] orgId={}", orgId);
        return response;
    }

    /**
     * Get Subscription Plan by Organization
     */
    @Operation(summary = "Get Module Subscription Plan by Organization Id")
    @GetMapping
    public SharkdomApiResponse<ModuleSubscriptionPlanResponse> getSubscriptionByOrgId() {

        Long orgId = Util.getOrgIdFromToken();
        log.info("[FETCH] Subscription Plan | orgId={}", orgId);

        SharkdomApiResponse<ModuleSubscriptionPlanResponse> response =
                service.getSubscriptionByOrgId(orgId);

        log.info("[FETCH-SUCCESS] orgId={}", orgId);
        return response;
    }
}
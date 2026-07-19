package com.sharkdom.subscription.controller;

import com.sharkdom.dto.AddressContactResponse;
import com.sharkdom.dto.CreateSetupIntentRequest;
import com.sharkdom.dto.SetupIntentResponse;
import com.sharkdom.model.stripe.*;
import com.sharkdom.profilesection.dto.BillingSettingsCombinedResponse;
import com.sharkdom.service.organization.SettingSectionService;
import com.sharkdom.service.stripe.StripeCardMethodService;
import com.sharkdom.service.stripe.StripeCustomerCheckoutServiceImpl;
import com.sharkdom.service.stripe.StripeCustomerService;
import com.sharkdom.service.stripe.StripeSubscriptionService;
import com.sharkdom.subscription.model.*;
import com.sharkdom.subscription.service.FeatureAccessService;
import com.sharkdom.subscription.service.ModuleSubscriptionPlanService;
import com.sharkdom.subscription.service.OrgSubscriptionService;
import com.sharkdom.subscription.service.ProductService;

import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingRestController {

    private final StripeCardMethodService cardService;
    private final StripeCustomerService customerService;
    private final StripeSubscriptionService stripeSubscriptionService;
    private final ProductService productService;
    private final ModuleSubscriptionPlanService moduleSubscriptionPlanService;
    private final FeatureAccessService featureAccessService;
    private final OrgSubscriptionService subscriptionService;
    private final StripeCustomerCheckoutServiceImpl customerCheckoutService;
    private final SettingSectionService settingSectionService;

    // ===================== CUSTOMER =====================

    @Operation(summary = "Create Stripe Customer for User")
    @PostMapping("/customer")
    public ResponseEntity<StripeCustomerDto> createCustomer(
            @RequestBody Map<String, String> request
    ) throws Exception {
        String userId = request.get("userId");
        StripeCustomerDto response = customerCheckoutService.createCustomer(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Stripe Customer by ID")
    @GetMapping("/customers/{customerId}")
    public SharkdomApiResponse<StripeCustomerDto> getCustomer(
            @PathVariable String customerId) throws StripeException {

        log.info("[FETCH CUSTOMER] customerId={}", customerId);

        StripeCustomerDto response = customerService.getCustomerByCustomerId(customerId);

        return new SharkdomApiResponse<>(true, "Customer fetched successfully", response);
    }

    // ===================== PAYMENT METHODS =====================

    @Operation(summary = "Save Customer Card Details")
    @PostMapping("/customers/{customerId}/payment-methods")
    @ResponseStatus(HttpStatus.CREATED)
    public SharkdomApiResponse<StripeCardDetailDto> saveCardDetails(
            @PathVariable String customerId) throws StripeException {

        log.info("[SAVE CARD] customerId={}", customerId);

        StripeCardDetailDto response =
                cardService.getAndSaveCustomerCardDetails(customerId);

        return new SharkdomApiResponse<>(true, "Card details saved", response);
    }

    @Operation(summary = "Update Default Payment Method")
    @PutMapping("/customers/{customerId}/payment-methods/{paymentMethodId}")
    public SharkdomApiResponse<StripeCardDetailDto> updatePaymentMethod(
            @PathVariable String customerId,
            @PathVariable String paymentMethodId) throws StripeException {

        log.info("[UPDATE CARD] customerId={} | paymentMethodId={}", customerId, paymentMethodId);

        StripeCardDetailDto response =
                cardService.updatePaymentMethod(customerId, paymentMethodId);

        return new SharkdomApiResponse<>(true, "Payment method updated", response);
    }

    @Operation(summary = "Create Setup Intent")
    @PostMapping("/setup-intents")
    public SharkdomApiResponse<SetupIntentResponse> createSetupIntent(
            @Valid @RequestBody CreateSetupIntentRequest request) throws StripeException {

        log.info("[SETUP INTENT] customerId={}", request.getCustomerId());

        SetupIntentResponse response =
                cardService.createSetupIntent(request.getCustomerId());

        return new SharkdomApiResponse<>(true, "Setup intent created", response);
    }

    // ===================== SUBSCRIPTIONS =====================

    @Operation(summary = "Create Subscription")
    @PostMapping("/subscriptions")
    @ResponseStatus(HttpStatus.CREATED)
    public SharkdomApiResponse<StripeSubscriptionDataDto> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) throws StripeException {

        log.info("[CREATE SUBSCRIPTION] customerId={} | priceId={}",
                request.getCustomerId(), request.getAmount());

        StripeSubscriptionDataDto response =
                stripeSubscriptionService.createSubscription(request);

        return new SharkdomApiResponse<>(true, "Subscription created successfully", response);
    }

    // ===================== PRODUCTS =====================

    @Operation(summary = "Get Products by Module Names")
    @PostMapping("/products/by-modules")
    public SharkdomApiResponse<List<ProductResponseDTO>> getProductsByModules(
            @RequestBody ModuleNameRequestDTO dto) {

        log.info("[FETCH PRODUCTS] modules={}", dto.getModuleNames());

        List<ProductResponseDTO> response =
                productService.getProductsByModuleNames(dto.getModuleNames());

        return new SharkdomApiResponse<>(true, "Products fetched successfully", response);
    }

    // ===================== FEATURE ACCESS =====================

    @Operation(summary = "Check Feature Access for Organization")
    @PostMapping("/feature-access")
    public SharkdomApiResponse<AccessResponse> checkFeatureAccess(
            @RequestBody FeatureAccessRequest request) {
        Long orgId = Util.getOrgIdFromToken();
        log.info("[API ACCESS CHECK] orgId={} | suite={} | freeFeature={}",
                orgId, request.getSuiteKey(), request.isFreeFeature());
        AccessResponse response = featureAccessService.canAccess(
                orgId,
                request.getSuiteKey(),
                request.isFreeFeature()
        );
        log.info("[API ACCESS RESULT] orgId={} | allowed={} | reason={}",
                orgId, response.isAllowed(), response.getReason());
        return new SharkdomApiResponse<>(
                true,
                "Feature access evaluated successfully",
                response
        );
    }


    // ===================== SUBSCRIPTION SUMMARY =====================

    @Operation(summary = "Get Current Subscription Summary")
    @GetMapping("/subscription/summary")
    public SharkdomApiResponse<SubscriptionSummaryResponse> getSubscriptionSummary() {

        Long orgId = Util.getOrgIdFromToken();

        log.info("[API SUBSCRIPTION SUMMARY] orgId={}", orgId);

        SubscriptionSummaryResponse response =
                subscriptionService.getSubscriptionSummary(orgId);

        return new SharkdomApiResponse<>(
                true,
                "Subscription summary fetched successfully",
                response
        );
    }

    @Operation(summary = "Get Billing History (Invoices)")
    @GetMapping("/invoices")
    public SharkdomApiResponse<List<InvoiceResponseDTO>> getBillingHistory() {

        Long orgId = Util.getOrgIdFromToken();

        log.info("[API INVOICES] orgId={}", orgId);

        List<InvoiceResponseDTO> response =
                subscriptionService.getInvoices(orgId);

        return new SharkdomApiResponse<>(
                true,
                "Invoices fetched successfully",
                response
        );
    }

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
                moduleSubscriptionPlanService.upsertSubscriptionPlan(request, orgId);

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
                moduleSubscriptionPlanService.getSubscriptionByOrgId(orgId);

        log.info("[FETCH-SUCCESS] orgId={}", orgId);
        return response;
    }

    /**
     * Update Subscription Plan by Organization
     */
    @Operation(summary = "Update Module Subscription Plan by Organization Id")
    @PutMapping
    public SharkdomApiResponse<ModuleSubscriptionPlanResponse> updateSubscriptionPlan(
            @RequestBody UpdateModuleSubscriptionPlanRequest request) {

        Long orgId = Util.getOrgIdFromToken();
        log.info("[UPDATE] Subscription Plan | orgId={}", orgId);

        SharkdomApiResponse<ModuleSubscriptionPlanResponse> response =
                moduleSubscriptionPlanService.updateSubscriptionPlan(orgId, request);

        log.info("[UPDATE-SUCCESS] orgId={}", orgId);
        return response;
    }

    @Operation(summary = "Get Combined Billing Settings Data")
    @GetMapping("/billing/overview")
    public ResponseEntity<SharkdomApiResponse<BillingSettingsCombinedResponse>> getBillingOverview() {

        Long orgId = Util.getOrgIdFromToken();

        log.info("[API BILLING OVERVIEW] orgId={}", orgId);

        BillingSettingsCombinedResponse response = getBillingSettingsData(orgId);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Billing overview fetched successfully",
                        response
                )
        );
    }

    public BillingSettingsCombinedResponse getBillingSettingsData(Long orgId) {

        log.info("[FETCH COMBINED BILLING DATA] orgId={}", orgId);

        AddressContactResponse addressContact =
                settingSectionService.getAddressAndContact();

        ModuleSubscriptionPlanResponse subscriptionPlan =
                moduleSubscriptionPlanService
                        .getSubscriptionByOrgId(orgId)
                        .getData();

        SubscriptionSummaryResponse subscriptionSummary =
                subscriptionService.getSubscriptionSummary(orgId);

        List<InvoiceResponseDTO> invoices =
                subscriptionService.getInvoices(orgId);

        return BillingSettingsCombinedResponse.builder()
                .addressContact(addressContact)
                .subscriptionPlan(subscriptionPlan)
                .subscriptionSummary(subscriptionSummary)
                .invoices(invoices)
                .build();
    }


    @Operation(summary = "Convert Free Trial To Module Subscription")
    @PostMapping("/convert/email/{email}")
    public SharkdomApiResponse<ModuleSubscriptionPlanResponse> convertFreeTrialToPaidPlan(
            @PathVariable String email) {

        log.info("[CONVERT] Free Trial To Paid Plan | email={}", email);

        return moduleSubscriptionPlanService.convertFreeTrialToPaidPlan(email);
    }

}
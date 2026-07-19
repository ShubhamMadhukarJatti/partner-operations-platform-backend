package com.sharkdom.controller.payment;

import com.sharkdom.constants.PlanType;
import com.sharkdom.entity.configuration.Configuration;
import com.sharkdom.entity.payment.StripEntity;
import com.sharkdom.entity.subscription.Subscription;
import com.sharkdom.model.payment.StripeCheckoutRequest;
import com.sharkdom.model.stripe.StripeCustomerDto;
import com.sharkdom.model.subscription.CreateSubscription;
import com.sharkdom.repository.payment.StripeCallbackRepository;
import com.sharkdom.service.payment.PaymentService;
import com.sharkdom.service.stripe.StripeCustomerCheckoutServiceImpl;
import com.sharkdom.service.stripe.StripeCustomerServiceImpl;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController

@CrossOrigin
@Slf4j
@RequestMapping("/payment")
public class PaymentController {
    private final PaymentService paymentService;
    private final StripeCallbackRepository stripeCallbackRepository;
    private final StripeCustomerCheckoutServiceImpl stripeCustomerService;


    public PaymentController(PaymentService paymentService, StripeCallbackRepository stripeCallbackRepository, StripeCustomerServiceImpl stripeCustomerService, StripeCustomerCheckoutServiceImpl stripeCustomerService1) {
        this.paymentService = paymentService;
        this.stripeCallbackRepository = stripeCallbackRepository;
        this.stripeCustomerService = stripeCustomerService1;
    }

    @Operation(summary = "Payment callback endpoint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Callback handled successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(path = "/callback")
    public Subscription handleCallback(HttpServletRequest request, @RequestBody Map<Object, Object> payment) {
        log.info("inside payment" + payment.toString());
        return paymentService.savePaymentDetails(request, payment);
    }


    @Operation(summary = "Create User Subscription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription create successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(path = "/subscription")
    public Map<String, String> createUserSubscription(@RequestBody CreateSubscription createSubscription) {
        return paymentService.createSubscription(createSubscription);
    }

    @Operation(summary = "Update Pricing")
    @PostMapping(path = "/pricing")
    public Configuration changePricing(@RequestParam PlanType planType, @RequestParam String amount) {
        return paymentService.updatePricing(planType, amount);
    }

    @Operation(summary = "Stripe callback endpoint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Callback handled successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(path = "/stripe/callback")
    public Subscription stripeCallback(HttpServletRequest request, @RequestBody Map<String, Object> payment) {
        log.info("inside payment" + payment.toString());
        StripEntity stripEntity = StripEntity.builder()
                .details(payment.toString()).build();
        stripeCallbackRepository.save(stripEntity);
        return paymentService.handleStripePaymentEvent(payment);
    }

    @PostMapping("/stripe/checkout-session")
    public ResponseEntity<Map<String, Object>> createCheckoutSession(@RequestBody StripeCheckoutRequest request) throws StripeException {
        return paymentService.createStripeCheckoutSession(request);
    }

    @PostMapping("/customer")
    @Operation(summary = "create checkout session by UserId and PlanType")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "create customer and checkout session by UserId and PlanType", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StripeCustomerDto.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    public ResponseEntity<StripeCustomerDto> createCustomer(
            @RequestBody Map<String, String> request
    ) throws Exception {
        String userId = request.get("userId");
        StripeCustomerDto response = stripeCustomerService.createCustomer(userId);
        return ResponseEntity.ok(response);
    }

}

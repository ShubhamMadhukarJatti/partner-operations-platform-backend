package com.sharkdom.controller.stripe;

import com.sharkdom.constants.stripe.StripeMode;
import com.sharkdom.constants.stripe.StripePlanType;
import com.sharkdom.model.stripe.StripeCheckoutSessionsDto;
import com.sharkdom.model.stripe.StripeCustomerDto;
import com.sharkdom.service.stripe.StripeCustomerCheckoutService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/sharkdom-stripe/v1")
@RequiredArgsConstructor
public class StripeCustomerCheckoutController {

    private final StripeCustomerCheckoutService customerCheckoutService;

    @Operation(summary = "create checkout session by UserId and PlanType")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "create customer and checkout session by UserId and PlanType", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StripeCustomerDto.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/customer-checkout")
    public ResponseEntity<StripeCheckoutSessionsDto> createCustomerAndCheckoutSession(@RequestParam(value = "userId") @Valid String userId,
                                                                                      @RequestParam(value = "planType") @Valid StripePlanType planType,
                                                                                      @RequestParam(value = "mode") @Valid StripeMode mode,
                                                                                      @RequestParam(value = "trailDays", required = false) @Valid Long trailDays,
                                                                                      @RequestParam(value = "successUrl") @Valid String successUrl,
                                                                                      @RequestParam(value = "cancelUrl") @Valid String cancelUrl,
                                                                                      @RequestParam(value = "couponCode", required = false) @Valid String couponCode,
                                                                                      @RequestParam(value = "isBusinessCustomer", required = false, defaultValue = "false") @Valid boolean isBusinessCustomer) throws StripeException {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerCheckoutService.createCustomerAndCheckoutSession(userId, planType, mode, trailDays, successUrl, cancelUrl, couponCode, isBusinessCustomer));
    }
}

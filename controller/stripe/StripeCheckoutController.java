package com.sharkdom.controller.stripe;

import com.sharkdom.model.stripe.StripeCheckoutSessionsDto;
import com.sharkdom.service.stripe.StripeCheckoutService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sharkdom-stripe/v1")
@RequiredArgsConstructor
public class StripeCheckoutController {

    private final StripeCheckoutService stripeCheckoutService;

//    @Operation(summary = "create checkout session ")
//    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "create checkout sessions.", content = {
//            @Content(mediaType = "application/json", schema = @Schema(implementation = StripeCheckoutSessionsDto.class))}),
//            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
//    @PostMapping("/checkout-session")
//    public ResponseEntity<StripeCheckoutSessionsDto> createCheckoutSession(@RequestBody @Valid StripeCheckoutSessionsDto stripeCheckoutSessionsDto) throws StripeException, InterruptedException {
//        return ResponseEntity.ok(stripeCheckoutService.createCheckoutSession(stripeCheckoutSessionsDto));
//    }

    @Operation(summary = "get checkout session ")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "get checkout sessions.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StripeCheckoutSessionsDto.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/checkout-session/{checkoutSessionId}")
    public ResponseEntity<StripeCheckoutSessionsDto> getCheckoutSessionById(@PathVariable(value = "checkoutSessionId") @Valid String checkoutSessionId) throws StripeException, InterruptedException {
        return ResponseEntity.ok(stripeCheckoutService.getCheckoutSessionById(checkoutSessionId));
    }

}

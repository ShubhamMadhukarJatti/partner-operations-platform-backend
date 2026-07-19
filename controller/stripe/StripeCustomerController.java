package com.sharkdom.controller.stripe;

import com.sharkdom.model.stripe.StripeCustomerDto;
import com.sharkdom.service.stripe.StripeCustomerService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sharkdom-stripe/v1")
@RequiredArgsConstructor
public class StripeCustomerController {

    private final StripeCustomerService stripeCustomerService;

    @PostMapping(value = "/customer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StripeCustomerDto> createCustomer(
            @RequestBody @Valid StripeCustomerDto stripeCustomerDto) throws StripeException {
        log.info("StripeCustomerController.createCustomer");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stripeCustomerService.createCustomer(stripeCustomerDto));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "get customer by customerId")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "get customer by customerId.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StripeCustomerDto.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    public ResponseEntity<StripeCustomerDto> getCustomerByCustomerId(@PathVariable(value = "customerId") @Valid String customerId) throws StripeException {
        return ResponseEntity.ok(stripeCustomerService.getCustomerByCustomerId(customerId));
    }
}

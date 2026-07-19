package com.sharkdom.controller.stripe;

import com.sharkdom.model.stripe.StripeCustomerDto;
import com.sharkdom.model.stripe.StripeInvoiceDto;
import com.sharkdom.service.stripe.StripeInvoiceService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sharkdom-stripe/v1")
@RequiredArgsConstructor
public class StripeInvoiceController {

    private final StripeInvoiceService stripeInvoiceService;

    @GetMapping("/invoice/{invoiceId}")
    @Operation(summary = "get invoice by invoiceId")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "get invoice by invoiceId.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StripeCustomerDto.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    public ResponseEntity<StripeInvoiceDto> getInvoiceByInvoiceId(@PathVariable(value = "invoiceId") @Valid String invoiceId) throws StripeException {
        return ResponseEntity.ok(stripeInvoiceService.getInvoiceByInvoiceId(invoiceId));
    }

    @GetMapping("/invoice/subscription/{subscriptionId}")
    @Operation(summary = "get all invoice by subscriptionId")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "get all invoice by subscriptionId.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = StripeCustomerDto.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    public ResponseEntity<List<StripeInvoiceDto>> getAllInvoiceBySubscriptionId(@PathVariable(value = "subscriptionId") @Valid String subscriptionId) throws StripeException {
        return ResponseEntity.ok(stripeInvoiceService.getAllInvoiceBySubscriptionId(subscriptionId));
    }

}

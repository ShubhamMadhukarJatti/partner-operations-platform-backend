package com.sharkdom.controller.stripe;

import com.sharkdom.constants.stripe.StripePlanType;
import com.sharkdom.model.stripe.StripePlanConfigurationRequest;
import com.sharkdom.model.stripe.StripePlanConfigurationResponse;
import com.sharkdom.model.stripe.StripeSubscriptionDataDto;
import com.sharkdom.service.stripe.StripePlanConfigurationService;
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
@RequiredArgsConstructor
@RequestMapping("/sharkdom-stripe/v1")
public class StripePlanConfigurationController {

    private final StripePlanConfigurationService stripePlanConfigurationService;

    @Operation(summary = "add plan configuration")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "add plan configuration",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = StripePlanConfigurationResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/plan-configuration")
    ResponseEntity<StripePlanConfigurationResponse> addStripePlanConfiguration(@RequestParam(value = "planType") StripePlanType stripePlanType, @RequestBody StripePlanConfigurationRequest stripePlanConfigurationRequest) throws StripeException {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(stripePlanConfigurationService.addStripePlanConfiguration(stripePlanType, stripePlanConfigurationRequest));
    }

    @Operation(summary = "update plan configuration")
    @ApiResponses(value = {@ApiResponse(responseCode = "202", description = "update plan subscription",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = StripePlanConfigurationResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PutMapping("/plan-configuration")
    ResponseEntity<StripePlanConfigurationResponse> updateStripePlanConfiguration(@RequestParam(value = "planType") StripePlanType stripePlanType, @RequestBody StripePlanConfigurationRequest stripePlanConfigurationRequest) throws StripeException {
        return ResponseEntity.status(HttpStatus.CREATED).body(stripePlanConfigurationService.updateStripePlanConfiguration(stripePlanType, stripePlanConfigurationRequest));
    }

    @Operation(summary = "get all plan configuration")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "get all plan configuration",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = StripePlanConfigurationResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/all-plan-configuration")
    ResponseEntity<List<StripePlanConfigurationResponse>> getAllStripePlanConfiguration() {
        return ResponseEntity.status(HttpStatus.OK).body(stripePlanConfigurationService.getAllStripePlanConfiguration());
    }

    @Operation(summary = "get plan configuration")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "get plan configuration",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = StripePlanConfigurationResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/plan-configuration")
    ResponseEntity<StripePlanConfigurationResponse> getStripePlanConfiguration(@RequestParam(value = "planType") StripePlanType stripePlanType) {
        return ResponseEntity.status(HttpStatus.OK).body(stripePlanConfigurationService.getStripePlanConfiguration(stripePlanType));
    }

    @Operation(summary = "get plan configuration price id for corresponding plan type")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "get plan configuration price id for corresponding plan type",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/plan-configuration/retrieve-price-id")
    ResponseEntity<Map<String, Object>> getPriceIdByPlanType(@RequestParam(value = "planType") StripePlanType stripePlanType) {
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("priceId", stripePlanConfigurationService.getPriceIdByPlanType(stripePlanType)));
    }

    @Operation(summary = "update plan configuration price id for corresponding plan type")
    @ApiResponses(value = {@ApiResponse(responseCode = "202", description = "update plan configuration price id for corresponding plan type",
            content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = StripePlanConfigurationResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PatchMapping("/plan-configuration/update-price-id")
    ResponseEntity<StripePlanConfigurationResponse> updatePriceIdByPlanType(@RequestParam(value = "planType") StripePlanType stripePlanType, @RequestParam(value = "priceId") String priceId) throws StripeException {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(stripePlanConfigurationService.updatePriceIdByPlanType(stripePlanType, priceId));
    }

}

package com.sharkdom.controller.integration;

import com.sharkdom.entity.integration.PartnershipIntegration;
import com.sharkdom.entity.notification.Notification;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.model.ai.ModeSaveRequest;
import com.sharkdom.model.integration.PartnershipIntegrationRequest;
import com.sharkdom.service.integration.PartnershipIntegrationService;
import com.sharkdom.zoho.dto.ZohoWebhookDetailsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController

@RequestMapping("/partnership-integration")
public class PartnershipIntegrationController {
    private final PartnershipIntegrationService partnershipIntegrationService;

    public PartnershipIntegrationController(PartnershipIntegrationService partnershipIntegrationService) {
        this.partnershipIntegrationService = partnershipIntegrationService;
    }

    @Operation(summary = "Save partnership integration details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Partnership integration saved successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PartnershipIntegration.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping()
    public PartnershipIntegration saveIntegration(@RequestBody PartnershipIntegrationRequest integrationRequest) {
        return partnershipIntegrationService.saveIntegration(integrationRequest);
    }

    @Operation(summary = "Get partnership integration details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Partnership integration found successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Notification.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping()
    public PartnershipIntegration getIntegration(@RequestParam Long organizationId) {
        return partnershipIntegrationService.getById(organizationId);
    }

    @Operation(summary = "Save Partner Listing Mode")
    @PostMapping(path = "/mode")
    public Map<String, String> saveMode(@RequestBody ModeSaveRequest modeSaveRequest) {
        return partnershipIntegrationService.saveModeDetails(modeSaveRequest);
    }
}

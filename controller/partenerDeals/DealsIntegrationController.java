package com.sharkdom.controller.partenerDeals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.constants.partnerDeals.DealStage;
import com.sharkdom.constants.partnerDeals.DealStatus;
import com.sharkdom.dto.DealResponseDto;
import com.sharkdom.model.partnerDeals.DealCheckResponse;
import com.sharkdom.model.partnerDeals.DealRequestDto;
import com.sharkdom.entity.partenearDeals.Deal;
import com.sharkdom.offlinePartner.model.OfflinePartnerInviteRequest;
import com.sharkdom.offlinePartner.service.OfflinePartnerService;
import com.sharkdom.salesforce.service.SalesforceSyncService;
import com.sharkdom.service.partenerDeals.DealService;
import com.sharkdom.service.partenerDeals.hubspot.HubSpotSyncService;
import com.sharkdom.service.partenerDeals.hubspot.dto.DealStatusCountDto;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.zoho.service.ZohoDealSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/my-deals")
public class DealsIntegrationController {

    @Autowired
    private DealService dealService;

    @Autowired
    private HubSpotSyncService hubSpotSyncService;

    @Autowired
    private OfflinePartnerService offlinePartnerService;

    @Autowired
    private ZohoDealSyncService zohoDealSyncService;

    @Autowired
    private SalesforceSyncService salesforceSyncService;

    @PostMapping()
    @Operation(summary = "Create Deal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Create Deal", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Deal.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    public ResponseEntity<Deal> createDeal(@RequestBody DealRequestDto deal) {
        Deal created = dealService.createDeal(deal);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/external/partner/portal")
    @Operation(summary = "Create Deal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Create Deal", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Deal.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    public ResponseEntity<Deal> createDealExternalPartnerPortal(@RequestBody DealRequestDto deal) {
        Deal created = dealService.createDealExternalPartnerPortal(deal);
        return ResponseEntity.ok(created);
    }

    @PatchMapping("/approve/{dealId}")
    @Operation(summary = "Approve Deal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Approve Deal", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Deal.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    public ResponseEntity<Deal> updateApproval(
            @PathVariable String dealId,
            @RequestParam Boolean isApproved,
            @RequestParam Long dealProtectionPeriod,
            @RequestParam IntegrationType integrationType) throws JsonProcessingException {

        Deal updated = dealService.updateApprovalStatus(dealId, isApproved, dealProtectionPeriod,integrationType);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update Deal details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update Deal details", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Deal.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    public ResponseEntity<Deal> updateFields(@PathVariable Long id, @RequestBody DealRequestDto dto) {
        Deal updated = dealService.updateDealFields(id, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/get/all")
    @Operation(summary = "Get All Deals by Stage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched all related deals", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = JSONObject.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<String> getAllRelatedDeals(@RequestParam("status") DealStatus status) throws JSONException {
        JSONObject response = dealService.getAllRelatedDeals(status);
        return ResponseEntity.ok(response.toString());
    }


    @GetMapping("/dealExist")
    @Operation(summary = "Check Deal Existance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get Partner details", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Deal.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    public ResponseEntity<DealCheckResponse> checkDealExists(
            @RequestParam Long vendorOrgId,
            @RequestParam String website) {
        DealCheckResponse response = dealService.checkDealExistence(vendorOrgId, website);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{hubSpotDealId}/history")
    @Operation(summary = "Get HubSpot Deal History by Deal ID",
            description = "Fetches the history of a HubSpot deal using its ID. Requires access token and a flag indicating if the requester is a vendor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched deal history", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized or invalid token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<String> getDealHistory(@PathVariable String hubSpotDealId,
                                                 @RequestParam boolean isVendor) {
        String dealHistory = hubSpotSyncService.fetchDealByIdWithHistory(hubSpotDealId, isVendor);
        return ResponseEntity.ok(dealHistory);
    }

    @GetMapping("/getDealsCount")
    @Operation(summary = "Get Deal Counts by Status for Organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched deal counts successfully", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DealStatusCountDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized or invalid token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<List<DealStatusCountDto>> getDealsCount(@RequestParam(required = true) long organizationId) {
        List<DealStatusCountDto> counts = dealService.getDealCountsByStatusForOrg(organizationId);
        return ResponseEntity.ok(counts);
    }

    @GetMapping("zoho/{zohoDealId}/history")
    @Operation(summary = "Get zoho Deal History by Deal ID",
            description = "Fetches the history of a zoho deal using its ID. Requires access token and a flag indicating if the requester is a vendor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched deal history", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized or invalid token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<?> getZohoDealHistory(@PathVariable String zohoDealId) {
        Map<String, Object> zohoDealHistory = zohoDealSyncService.getZohoDealHistory(zohoDealId);
        return ResponseEntity.ok(zohoDealHistory);
    }

    @Operation(summary = "Invite Offline Partners for partner deal portal")
    @PostMapping("/external/partner/portal/invite")
    public List<Map<String, String>> invitePartners(@RequestBody OfflinePartnerInviteRequest offlinePartnerInviteRequest) {
        return dealService.invitePartnersExternalPartnerPortal(offlinePartnerInviteRequest);
    }

    @GetMapping("/salesforce/{salesforceDealId}/history")
    @Operation(summary = "Get salesforce Deal History by Deal ID",
            description = "Fetches the history of a salesforce deal using its ID. Requires access token and a flag indicating if the requester is a vendor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched deal history", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))
            }),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized or invalid token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<?> getSalesforceDealHistory(@PathVariable String salesforceDealId) {
        Map<String, Object> salesForceDealHistory = salesforceSyncService.getSalesForceDealHistory(salesforceDealId);
        return ResponseEntity.ok(salesForceDealHistory);
    }


    @GetMapping("/external/partner/portal/get/all/deals/{userId}/organization/{vendorOrgId}")
    public ResponseEntity<SharkdomApiResponse<Page<Deal>>> getDealsByUserAndStatus(
            @PathVariable String userId,
            @PathVariable Long vendorOrgId,
            @RequestParam DealStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Deal> deals = dealService.getDealsByUserAndStatus(userId, status,vendorOrgId ,page, size);
        SharkdomApiResponse<Page<Deal>> response =
                new SharkdomApiResponse<>(true, "Deals fetched successfully", deals);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/external/partner/portal/deal/exist")
    @Operation(summary = "Check Deal Existance for external partner portal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get Partner details", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Deal.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    public ResponseEntity<DealCheckResponse> checkDealExistsEPS(
            @RequestParam String externalPartnerCode,
            @RequestParam String website) {
        DealCheckResponse response = dealService.checkDealExistenceForEPS(externalPartnerCode, website);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/external/partner/portal/getDealsCount/{vendorOrgId}")
    @Operation(summary = "Get Deal Counts by Status for Organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched deal counts successfully", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DealStatusCountDto.class))
            }),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized or invalid token", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public ResponseEntity<List<DealStatusCountDto>> getDealsCountExternalPartnerPortalByExternalPartnerPortal(@RequestParam(required = true) String externalPartnerCode,@PathVariable Long vendorOrgId ) {
        List<DealStatusCountDto> counts = dealService.getDealCountsByExternalPartnerCode(externalPartnerCode, vendorOrgId);
        return ResponseEntity.ok(counts);
    }

    @PostMapping("/internal/external/partner")
    @Operation(summary = "Create Deal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Create Deal", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Deal.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    public ResponseEntity<Deal> createDealInternalExternalPartnerPortal(@RequestBody DealRequestDto deal) {
        Deal created = dealService.createDealInternalToExternalPartnerPortal(deal);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/external/partner/portal/get/deals/{externalPartnerCode}/organization/{vendorOrgId}")
    public ResponseEntity<SharkdomApiResponse<Page<Deal>>> getDealsByUserAndStatusByExternalPartnerCode(
            @PathVariable String externalPartnerCode,
            @PathVariable Long vendorOrgId,
            @RequestParam DealStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Deal> deals = dealService.getDealsInExternalPartnerPortalSend(externalPartnerCode, status,vendorOrgId ,page, size);
        SharkdomApiResponse<Page<Deal>> response =
                new SharkdomApiResponse<>(true, "Deals fetched successfully", deals);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/external/partner/portal/get/deals/received/{externalPartnerCode}/organization/{vendorOrgId}")
    public ResponseEntity<SharkdomApiResponse<Page<Deal>>> getDealsByExternalPartnerCode(
            @PathVariable String externalPartnerCode,
            @PathVariable Long vendorOrgId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Deal> deals = dealService.getDealsInExternalPartnerPortalReceived(externalPartnerCode,vendorOrgId ,page, size);
        SharkdomApiResponse<Page<Deal>> response =
                new SharkdomApiResponse<>(true, "Deals fetched successfully", deals);
        return ResponseEntity.ok(response);
    }



    @PatchMapping("/external/partner/portal/approve/{dealId}")
    @Operation(summary = "Approve Deal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Approve Deal", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Deal.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    public ResponseEntity<Deal> updateApprovalExternalPartnerPortalToInternal(
            @PathVariable String dealId,
            @RequestParam Boolean isApproved,
            @RequestParam Long dealProtectionPeriod,
            @RequestParam IntegrationType integrationType) throws JsonProcessingException {

        Deal updated = dealService.updateApprovalStatusExternalPartnerPortalToInternal(dealId, isApproved, dealProtectionPeriod,integrationType);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/internal/partner/portal/approve/{dealId}")
    @Operation(summary = "Approve Deal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Approve Deal", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Deal.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    public ResponseEntity<Deal> updateApprovalInternalPartnerPortalToExternal(
            @PathVariable String dealId,
            @RequestParam Boolean isApproved,
            @RequestParam Long dealProtectionPeriod,
            @RequestParam IntegrationType integrationType) throws JsonProcessingException {

        Deal updated = dealService.updateApprovalStatusInternalPartnerPortalToExternal(dealId, isApproved, dealProtectionPeriod,integrationType);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/internal/received/deals")
    public ResponseEntity<Page<Deal>> getReceivedDeals(
            @RequestParam String externalPartnerCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<Deal> deals = dealService.getDealsInInternalPartnerReceived(
                externalPartnerCode,
                page,
                size
        );

        return ResponseEntity.ok(deals);
    }


    @GetMapping("/internal/sent/deals")
    public ResponseEntity<Page<Deal>> getSentDeals(
            @RequestParam String externalPartnerCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<Deal> deals = dealService.getDealsInInternalPartnerSend(
                externalPartnerCode,
                page,
                size
        );

        return ResponseEntity.ok(deals);
    }

}

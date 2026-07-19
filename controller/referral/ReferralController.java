package com.sharkdom.controller.referral;

import com.github.fge.jsonpatch.JsonPatch;
import com.sharkdom.entity.referral.CampaignEntity;
import com.sharkdom.entity.referral.LeadsEntity;
import com.sharkdom.model.referral.*;
import com.sharkdom.service.referral.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController

@CrossOrigin
@Slf4j
@RequestMapping("/referral")
public class ReferralController {
    private final ReferralService referralService;

    public ReferralController(ReferralService referralService) {
        this.referralService = referralService;
    }

    @Operation(summary = "Save impression")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Impression saved", content = {
                    @Content(mediaType = "application/json", schema = @Schema())})})
    @PostMapping("/impression")
    public void saveImpression(HttpServletRequest request, @RequestParam String referralCode) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        referralService.saveImpression(referralCode, ipAddress);
    }

    @Operation(summary = "Save lead")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lead saved", content = {
                    @Content(mediaType = "application/json", schema = @Schema())})})
    @PostMapping("/lead")
    public void saveLead(@RequestParam String referralCode, @RequestParam String email, @RequestParam(required = false) String name) {
        referralService.saveLead(referralCode, email, name);
    }

    @PatchMapping("/lead/status")
    public LeadsEntity updateLeadStatus(@RequestBody UpdateLeadStatus updateLeadStatus) {
        return referralService.updateLeadStatus(updateLeadStatus);
    }

    @Operation(summary = "Get referral data by referral code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lead saved", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ReferralData.class))})})
    @GetMapping("/data")
    public List<ReferralData> getReferralData(@RequestParam String referralCode,
                                              @Schema(defaultValue = "2024-05-07") @RequestParam(required = false) String from,
                                              @Schema(defaultValue = "2024-05-12") @RequestParam(required = false) String to) {
        return referralService.getReferralData(referralCode, from, to);
    }


    @Operation(summary = "Get leads data by referral code")
    @GetMapping("/leads/stats")
    public Page<LeadsStats> getLeadsData(@RequestParam String referralCode, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return referralService.getLeadsData(referralCode, page, size);
    }

    @Operation(summary = "Get leads data by referral code")
    @GetMapping("/campaign/stats")
    public Page<LeadsStats> getCampaignData(@RequestParam String referralCode, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return referralService.getLeadsData(referralCode, page, size);
    }


    @Operation(summary = "Get partner details")
    @GetMapping("/partners")
    public DashboardSummaryResponse getPartners(@RequestParam Long organizationId) {
        return referralService.getPartners(organizationId);
    }


    @GetMapping("/tested-campaign")
    public ReferralScriptCheckResponse testedCampaign(@RequestParam String referralCode, @RequestParam String website) {
        return referralService.testedCampaign(referralCode, website);
    }

    @Operation(summary = "Get partner details")
    @GetMapping("/{organizationId}/partner/{partnerId}")
    public ResponseEntity<PartnerDetailsResponse> getPartnerDetails(
            @PathVariable Long organizationId,
            @PathVariable Long partnerId) {
        PartnerDetailsResponse response = referralService.getPartnerDetails(organizationId, partnerId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get campaign data by referral code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campaign data", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CampaignEntity.class))})})
    @GetMapping("/campaign")
    public CampaignEntity getCampaignData(@RequestParam String referralCode) {
        return referralService.getCampaignData(referralCode);
    }

    @Operation(summary = "Get campaign data by organization id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campaign data", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CampaignResponse.class))})})
    @GetMapping("/campaigns")
    public CampaignResponse getCampaignsData(@RequestParam Long organizationId) {
        return referralService.getCampaignsData(organizationId);
    }

    @Operation(summary = "Get joined campaign data by organization id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campaign data", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CampaignResponse.class))})})
    @GetMapping("/campaigns-joined")
    public CampaignResponse getJoinedCampaigns(@RequestParam Long organizationId) {
        return referralService.getJoinedCampaignsData(organizationId);
    }

    @Operation(summary = "Create campaign")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campaign data", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CampaignEntity.class))})})
    @PostMapping("/campaign")
    public ReferralLinkResponse createCampaign(@RequestBody CampaignUpdateRequest campaignUpdateRequest) {
        return referralService.createCampaign(campaignUpdateRequest);
    }

    @PostMapping("/campaign/invite")
    public Map<String, String> invitePartner(@RequestBody InviteCampaignRequest inviteCampaignRequest) {
        referralService.invitePartner(inviteCampaignRequest);
        return Map.of("message", "Partner invited successfully");
    }

    @Operation(summary = "Use json patch to partially update campaign")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campaign updated successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CampaignEntity.class))}),
            @ApiResponse(responseCode = "404", description = "Campaign not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PatchMapping(path = "/campaign", consumes = "application/json-patch+json")
    public CampaignEntity patchCampaignById(@RequestParam(name = "id") long id,
                                            @Parameter(description = "MyDto") @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                                                    @ExampleObject(name = "Sending one replace and one add operation", value = "[\r\n"
                                                            + "    {\"op\":\"replace\",\"path\":\"/partnerOrganizationName\",\"value\":\"StarttupA\"},\r\n"
                                                            + "    {\"op\":\"replace\",\"path\":\"/partnerId\",\"value\":1}\r\n" + "]")})) @RequestBody JsonPatch patch)
            throws Exception {
        log.info("Received patch request with campaign id: " + id + " and json: " + patch);
        return referralService.patchById(id, patch);
    }
}

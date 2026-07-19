package com.sharkdom.profilesection.controllers;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.dto.PartnerPortalBrandingResponse;
import com.sharkdom.entity.ai.OverlapRecordEntity;
import com.sharkdom.entity.catalogue.dto.PricingTierListResponse;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organization.OrganizationCustomResponse;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.model.ai.RecordType;
import com.sharkdom.partnertraining.dto.CoverImageUploadResponseDto;
import com.sharkdom.partnertraining.service.CourseService;
import com.sharkdom.profilesection.dto.*;
import com.sharkdom.profilesection.service.PartnershipService;
import com.sharkdom.service.ai.PersonaService;
import com.sharkdom.service.catalogue.PricingTierService;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.service.organization.OrganizationService;
import com.sharkdom.profilesection.service.PartnerOrganizationService;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/partner-organizations")
@Tag(name = "Partner Organization APIs")
public class PartnerOrganizationController {

    private final OrganizationService organizationService;
    private final PartnerOrganizationService partnerOrganizationService;
    private final PricingTierService pricingTierService;
    private final CourseService courseService;
    private final PersonaService personaService;
    private final EmailService emailService;
    private final PartnershipService partnershipService;

    // ================= RECOMMENDATIONS =================

    @Operation(summary = "Get recommended organizations based on sectors")
    @GetMapping("/recommendations")
    public ResponseEntity<SharkdomApiResponse<Page<OrganizationCustomResponse>>> getRecommendations(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @RequestParam(required = false) String sectors,
            @RequestParam(defaultValue = "false") boolean matchAll
    ) {

        log.info("[GET_RECOMMENDATIONS] page={} size={} sectors={} matchAll={}",
                page, size, sectors, matchAll);

        List<String> filterList = parseFilters(sectors);

        Page<OrganizationCustomResponse> result =
                organizationService.searchOrganizationsByFilterDto(filterList, matchAll, page, size);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Recommendations fetched successfully",
                        result
                )
        );
    }

    // ================= SEARCH COUNT =================

    @Operation(summary = "Increment search count for an organization")
    @PostMapping("/{orgId}/search-count")
    public ResponseEntity<SharkdomApiResponse<Integer>> incrementSearchCount(
            @PathVariable Long orgId
    ) {

        log.info("[INCREMENT_SEARCH_COUNT] orgId={}", orgId);

        Integer count = partnerOrganizationService.incrementSearchCount(orgId);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Search count updated successfully",
                        count
                )
        );
    }

    // ================= ORGANIZATION INSIGHTS =================

    @Operation(summary = "Get partner organization insights")
    @GetMapping("/{orgId}/insights")
    public ResponseEntity<SharkdomApiResponse<PartnerOrganizationResponse>> getOrganizationInsights(
            @PathVariable Long orgId
    ) {

        log.info("[GET_ORG_INSIGHTS] orgId={}", orgId);

        PartnerOrganizationResponse response =
                partnerOrganizationService.getPartnerOrganizationDetails(orgId);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Partner organization details fetched successfully",
                        response
                )
        );
    }

    // ================= PRIVATE HELPERS =================

    private List<String> parseFilters(String filters) {

        if (filters == null || filters.isBlank()) {
            return List.of();
        }

        return List.of(filters.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .distinct()
                .toList();
    }

    @Operation(summary = "Get pricing tiers by organization ID")
    @GetMapping("/pricing-tiers/{orgId}")
    public ResponseEntity<SharkdomApiResponse<PricingTierListResponse>> getPricingTiersByOrg(
            @PathVariable Long orgId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
    ) {

        log.info("[GET_PRICING_TIERS] orgId={} page={} size={}", orgId, page, size);

        PricingTierListResponse response =
                pricingTierService.getPricingTiersByOrg(orgId, page, size);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Pricing tiers fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Upload file",
            description = "Uploads a file to S3 and returns the public file URL"
    )
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public SharkdomApiResponse<CoverImageUploadResponseDto> uploadFile(
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading file: name={}, size={}",
                file.getOriginalFilename(), file.getSize());
        CoverImageUploadResponseDto response =
                courseService.uploadFile(file);
        log.info("File uploaded successfully. URL={}", response.getFileUrl());
        return new SharkdomApiResponse<>(
                true,
                "File uploaded successfully",
                response
        );
    }

    @Operation(summary = "Update Organization Cover Image")
    @PutMapping("/cover-image")
    public SharkdomApiResponse<String> updateCoverImage(
            @Valid @RequestBody UpdateCoverImageRequest request) {

        return new SharkdomApiResponse<>(
                true,
                partnerOrganizationService.updateCoverImage(request),
                null
        );
    }

    @Operation(summary = "Update Organization Basic Profile")
    @PutMapping("/profile")
    public SharkdomApiResponse<String> updateOrganizationProfile(
            @Valid @RequestBody UpdateOrganizationProfileRequest request) {

        return new SharkdomApiResponse<>(
                true,
                partnerOrganizationService.updateOrganizationProfile(request),
                null
        );
    }

    @Operation(summary = "Get Organization Profile")
    @GetMapping("/profile/section")
    public SharkdomApiResponse<OrganizationProfileSectionResponse> getOrganizationProfileSection() {

        return new SharkdomApiResponse<>(
                true,
                "Organization profile fetched successfully",
                partnerOrganizationService.getOrganizationProfile()
        );
    }

    @Operation(summary = "Get Organization Profile")
    @GetMapping("/profile/{orgId}")
    public SharkdomApiResponse<OrganizationProfileSectionResponse> getOrganizationProfile(@PathVariable Long orgId) {

        return new SharkdomApiResponse<>(
                true,
                "Organization profile fetched successfully",
                partnerOrganizationService.getOrganizationProfileByOrgId(orgId)
        );
    }

    @Operation(summary = "Check CRM Integration Status")
    @GetMapping("/{orgId}/crm-status")
    public ResponseEntity<SharkdomApiResponse<CrmConnectionStatusResponse>> getCrmStatus(
            @PathVariable Long orgId
    ) {
        var orgIdFromToken = Util.getOrgIdFromToken();
        log.info("[GET_CRM_STATUS] orgId={}", orgId);

        boolean isConnected = partnerOrganizationService.hasAnyCrmIntegrationActive(orgId);
        boolean isYourConnected = partnerOrganizationService.hasAnyCrmIntegrationActive(orgIdFromToken);

        var response = CrmConnectionStatusResponse.builder()
                .isPartnerCrmConnected(isConnected)
                .isYourCrmConnected(isYourConnected)
                .build();

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "CRM status fetched successfully",
                        response
                )
        );
    }

    @Operation(summary = "Mark profile step as completed")
    @PostMapping("/{orgId}/profile-completion")
    public ResponseEntity<SharkdomApiResponse<String>> markProfileStepCompleted(
            @PathVariable Long orgId,
            @RequestParam ProfileCompletionType type
    ) {

        log.info("[MARK_PROFILE_STEP_COMPLETED] orgId={} type={}", orgId, type);

        partnerOrganizationService.markProfileStepCompleted(orgId, type);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Profile step marked as completed",
                        null
                )
        );
    }

    @Operation(summary = "Get profile completion status")
    @GetMapping("/{orgId}/profile-completion")
    public ResponseEntity<SharkdomApiResponse<ProfileCompletionStatusResponse>> getProfileCompletionStatus(
            @PathVariable Long orgId
    ) {

        log.info("[GET_PROFILE_COMPLETION_STATUS] orgId={}", orgId);

        ProfileCompletionStatusResponse response =
                partnerOrganizationService.getProfileCompletionStatus(orgId);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Profile completion status fetched successfully",
                        response
                )
        );
    }

    @Operation(summary = "Remove profile completion step")
    @DeleteMapping("/{orgId}/profile-completion")
    public ResponseEntity<SharkdomApiResponse<String>> removeProfileStep(
            @PathVariable Long orgId,
            @RequestParam ProfileCompletionType type
    ) {

        log.info("[REMOVE_PROFILE_STEP_API] orgId={} type={}", orgId, type);

        partnerOrganizationService.removeProfileStep(orgId, type);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Profile step removed successfully",
                        null
                )
        );
    }

    @Operation(summary = "Get Partner Program for Profile Section")
    @GetMapping("/{orgId}/partner-program")
    public ResponseEntity<PartnerPortalBrandingResponse> getBrandingForProfileSection(
            @PathVariable Long orgId
    ) {

        log.info("[GET_BRANDING_PROFILE_SECTION] orgId={}", orgId);

        PartnerPortalBrandingResponse response =
                partnerOrganizationService.getBrandingByOrgIdForProfileSection(orgId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Organization by ID with Collaborations")
    @GetMapping("/{orgId}/details")
    public ResponseEntity<Organization> getOrganizationById(
            @PathVariable Long orgId
    ) {

        log.info("[GET_ORGANIZATION_BY_ID] orgId={}", orgId);

        Organization organization = organizationService
                .findById(orgId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH08, orgId));

        return ResponseEntity.ok(organization);
    }

    @GetMapping("/overlap/my-records/org/{organizationId}")
    @Operation(summary = "Get overlap records for an organization with orgId path variable")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Records retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})
    public List<OverlapRecordEntity> getOverlapRecordsForOrg(
            @PathVariable Long organizationId,
            @RequestParam(required = false) RecordType recordType) {
        return personaService.getOverlapRecord(recordType,organizationId);
    }

    @GetMapping("/overlap/my-records")
    @Operation(summary = "Get overlap records for an organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Records retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})
    public List<OverlapRecordEntity> getOverlapRecords(
            @RequestParam(required = false) RecordType recordType) {
        return personaService.getOverlapRecords(recordType);
    }

    @GetMapping("/persona/overlap")
    @Operation(summary = "Get overlap records for an organization with other organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Records retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})
    public Map<String, Integer> getOverlapRecords(@RequestParam Long partnerId) {
        return personaService.getOverlapRecords(partnerId);
    }

    @Operation(summary = "Get organization stats (views, inquiries, rank, elite badge)")
    @GetMapping("/{orgId}/stats")
    public ResponseEntity<OrganizationStatsResponse> getOrganizationStats(
            @PathVariable Long orgId
    ) {

        log.info("[GET_ORG_STATS_API] orgId={}", orgId);

        OrganizationStatsResponse response =
                partnerOrganizationService.getOrganizationStats(orgId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Send Partner Interest Notification Email")
    @PostMapping("/send-partner-interest-email")
    public ResponseEntity<String> sendPartnerInterestEmail(
            @RequestParam String receiverEmail,
            @RequestParam(required = false) String receiverName
    ) throws Exception {
        log.info("[SEND_PARTNER_INTEREST_EMAIL] email={} ",
                receiverEmail);

        // fallback if name not provided
        if (receiverName == null || receiverName.isBlank()) {
            receiverName = receiverEmail.split("@")[0];
        }
        emailService.sendPartnerInterestNotificationEmail(
                "CRM_SETUP_REMINDER_FOR_PARTNERSHIP",
                receiverEmail,
                receiverName
        );

        return ResponseEntity.ok("Partner interest email sent successfully");
    }

    @Operation(summary = "Increment enquery counter")
    @PostMapping("/seniority-enquery-counter")
    public ResponseEntity<SharkdomApiResponse<Integer>> incrementSeniorityCounter() {
        Integer count = partnerOrganizationService.incrementSeniorityCounter();
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Seniority counter incremented successfully",
                        count
                )
        );
    }

    @Operation(summary = "Evaluate partnership compatibility between two startups")
    @PostMapping("/ai/evaluate")
    public ResponseEntity<SharkdomApiResponse<PartnershipResponse>> evaluatePartnership(
            @Valid @RequestBody EvaluateRequest request
    ) throws Exception {
        log.info("[AI_PARTNERSHIP_EVALUATION] sentence={}", request.getSentence());
        PartnershipResponse response =
                partnershipService.evaluate(request.getSentence());
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Partnership evaluation completed successfully",
                        response
                )
        );
    }

    @Operation(summary = "Get responder URL by organization ID")
    @GetMapping("/{orgId}/responder-url")
    public ResponseEntity<SharkdomApiResponse<String>> getResponderUrl(
            @PathVariable Long orgId) {
        log.info("[GET_RESPONDER_URL_API] orgId={}", orgId);
        String responderUrl = partnerOrganizationService.getResponderUrlByOrgId(orgId);
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Responder URL fetched successfully",
                        responderUrl
                )
        );
    }
}
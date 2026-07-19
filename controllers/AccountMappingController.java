package com.sharkdom.partnerattribution.controllers;

import com.sharkdom.dataenrichment.DataEnrichmentRequest;
import com.sharkdom.dataenrichment.DataEnrichmentResponse;
import com.sharkdom.dataenrichment.DataEnrichmentService;
import com.sharkdom.heathscore.CoSellHealthRequest;
import com.sharkdom.heathscore.CoSellHealthResponse;
import com.sharkdom.heathscore.CoSellHealthScoreService;
import com.sharkdom.partnerattribution.dto.*;
import com.sharkdom.partnerattribution.emails.dto.IntroGenerateRequest;
import com.sharkdom.partnerattribution.emails.dto.response.IntroGenerateResponse;
import com.sharkdom.partnerattribution.emails.service.IntroService;
import com.sharkdom.partnerattribution.service.*;
import com.sharkdom.service.ai.HubspotService;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.SharkdomPaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/account-mapping")
@RequiredArgsConstructor
@Tag(name = "Account Mapping Dashboard", description = "APIs for Account Overlap Dashboard")
public class AccountMappingController {

    private final AccountMappingService accountMappingService;
    private final NotificationService notificationService;
    private final VendorToPartnerOutreachService outreachService;
    private final AgreedNextStepService agreedNextStepService;
    private final JointPitchService jointPitchService;
    private final SharedAssetService sharedAssetService;
    private final ActivityLogService activityLogService;
    private final FileUploadService fileUploadService;
    private final SharedContactService sharedContactService;
    private final HubspotService hubspotService;
    private final DealMappingService dealMappingService;
    private final IntroService introService;
    private final DealOwnerDetailsService dealOwnerDetailsService;
    private final CoSellHealthScoreService coSellHealthScoreService;
    private final DataEnrichmentService dataEnrichmentService;


    @GetMapping("/summary")
    @Operation(summary = "Get overlap dashboard summary")
    public SharkdomApiResponse<AccountMappingSummaryDTO> getSummary() {

        AccountMappingSummaryDTO summary = accountMappingService.getAccountMappingSummary();

        return new SharkdomApiResponse<>(
                true,
                "Account mapping summary fetched successfully",
                summary
        );
    }

    @GetMapping("/shared-accounts/{partnerOrgId}")
    @Operation(summary = "Get shared accounts with partner")
    public SharkdomApiResponse<SharkdomPaginatedResponse<SharedAccountDTO>> getSharedAccounts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "score") String sort,
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(required = false) String search,
            @PathVariable Long partnerOrgId
    ) {

        SharkdomPaginatedResponse<SharedAccountDTO> result =
                dealMappingService.getSharedAccounts(
                        partnerOrgId,
                        page,
                        size,
                        sort,
                        filter,
                        search
                );

        return new SharkdomApiResponse<>(
                true,
                "Shared accounts fetched successfully",
                result
        );
    }

    @GetMapping("/{accountId}/co-sell-recommendation")
    @Operation(summary = "Fetch Co-sell Recommendation")
    public SharkdomApiResponse<CoSellRecommendationResponseDTO> getRecommendation(
            @PathVariable String accountId) {

        CoSellRecommendationResponseDTO response =
                accountMappingService.getCoSellRecommendation(accountId);

        return new SharkdomApiResponse<>(
                true,
                "Recommendation fetched successfully",
                response
        );
    }

    @PostMapping("/notifications/send")
    @Operation(
            summary = "Send or Schedule Notification",
            description = """
                This API allows sending notification immediately or scheduling it for later.

                Features:
                - Template based notification
                - Immediate or scheduled notification
                - Dynamic template data support
                - Multi channel support (Email, SMS, Push)

                Example Use Cases:
                - Payment reminder
                - Meeting reminder
                - OTP notification
                """
    )
    public SharkdomApiResponse<String> sendNotification(
            @RequestBody NotificationRequestDTO request) {

        notificationService.processNotification(request);

        return new SharkdomApiResponse<>(
                true,
                "Notification processed successfully",
                "SUCCESS"
        );
    }


    @PostMapping("/generate")
    @Operation(summary = "Generate Outreach Email (Vendor ↔ Partner ↔ Target)")
    public SharkdomApiResponse<OutreachResponse> generateOutreach(
            @RequestBody PartnerIntroductionDTO request) {
        log.info("Generating outreach | senderType={} | opportunityType={}",
                request.getSenderType(), request.getOpportunityType());
        try {

            OutreachResponse response = outreachService.generateOutreach(request);

            log.info("Outreach generated successfully | subject={}",
                    response != null ? response.getSubject() : null);

            return new SharkdomApiResponse<>(
                    true,
                    "Outreach email generated successfully",
                    response
            );

        } catch (Exception ex) {

            log.error("Error generating outreach email", ex);

            return new SharkdomApiResponse<>(
                    false,
                    "Failed to generate outreach email",
                    null
            );
        }
    }

    @PostMapping("/agreed-next-steps")
    @Operation(
            summary = "Create Agreed Next Step",
            description = "Create a new agreed next step for an organization"
    )
    public SharkdomApiResponse<AgreedNextStepResponseDto> createAgreedNextStep(
            @RequestBody AgreedNextStepRequestDto request) {

        log.info("API: Create AgreedNextStep | orgId={} | title={}",
                request.getOrgId(), request.getTitle());

        AgreedNextStepResponseDto response = agreedNextStepService.create(request);

        return new SharkdomApiResponse<>(
                true,
                "Agreed next step created successfully",
                response
        );
    }

    @PutMapping("/agreed-next-steps/{id}")
    @Operation(
            summary = "Update Agreed Next Step",
            description = "Update an existing agreed next step by ID"
    )
    public SharkdomApiResponse<AgreedNextStepResponseDto> updateAgreedNextStep(
            @PathVariable Long id,
            @RequestBody AgreedNextStepRequestDto request) {

        log.info("API: Update AgreedNextStep | id={}", id);

        AgreedNextStepResponseDto response = agreedNextStepService.update(id, request);

        return new SharkdomApiResponse<>(
                true,
                "Agreed next step updated successfully",
                response
        );
    }

    @DeleteMapping("/agreed-next-steps/{id}")
    @Operation(
            summary = "Delete Agreed Next Step",
            description = "Soft delete an agreed next step by ID"
    )
    public SharkdomApiResponse<String> deleteAgreedNextStep(
            @PathVariable Long id) {

        log.info("API: Delete AgreedNextStep | id={}", id);

        agreedNextStepService.delete(id);

        return new SharkdomApiResponse<>(
                true,
                "Agreed next step deleted successfully",
                "SUCCESS"
        );
    }

    @GetMapping("/agreed-next-steps/org/{orgId}/deal/{dealId}")
    @Operation(
            summary = "Get Agreed Next Steps by Org ID",
            description = "Fetch all agreed next steps for a given organization with pagination"
    )
    public SharkdomApiResponse<SharkdomPaginatedResponse<AgreedNextStepResponseDto>> getAgreedNextStepsByOrgId(

            @PathVariable Long orgId,
            @PathVariable String dealId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("API: Fetch AgreedNextSteps | orgId={} | page={} | size={}",
                orgId, page, size);

        SharkdomPaginatedResponse<AgreedNextStepResponseDto> response =
                agreedNextStepService.getByOrgId(orgId,dealId, page, size);

        return new SharkdomApiResponse<>(
                true,
                "Agreed next steps fetched successfully",
                response
        );
    }


    @PostMapping("/joint-pitch")
    @Operation(summary = "Create/Update Joint Pitch")
    public SharkdomApiResponse<JointPitchResponseDTO> saveJointPitch(
            @RequestBody JointPitchRequestDTO request) {

        log.info("API: Save JointPitch | partnerOrgId={}", request.getPartnerOrgId());

        return new SharkdomApiResponse<>(
                true,
                "Joint pitch saved successfully",
                jointPitchService.saveOrUpdate(request)
        );
    }

    @GetMapping("/joint-pitch/{partnerOrgId}/deal/{dealId}")
    @Operation(summary = "Get Joint Pitch")
    public SharkdomApiResponse<JointPitchResponseDTO> getJointPitch(
            @PathVariable Long partnerOrgId,
            @PathVariable String dealId) {

        log.info("API: Get JointPitch | partnerOrgId={}", partnerOrgId);

        return new SharkdomApiResponse<>(
                true,
                "Joint pitch fetched successfully",
                jointPitchService.get(partnerOrgId,dealId)
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
    public SharkdomApiResponse<FileUploadResponseDto> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "shared-assets") String folder
    ) {

        log.info("Uploading file | name={} | size={} | folder={}",
                file.getOriginalFilename(), file.getSize(), folder);

        FileUploadResponseDto response =
                fileUploadService.uploadFile(file, folder);

        log.info("File uploaded successfully | url={}", response.getFileUrl());

        return new SharkdomApiResponse<>(
                true,
                "File uploaded successfully",
                response
        );
    }

    @PostMapping("/shared-assets")
    @Operation(
            summary = "Upload Shared Asset",
            description = "Save shared asset with S3 URL"
    )
    public SharkdomApiResponse<SharedAssetResponseDTO> createAsset(
            @RequestBody SharedAssetRequestDTO request) {

        log.info("API: Create Shared Asset | partnerOrgId={}", request.getPartnerOrgId());

        return new SharkdomApiResponse<>(
                true,
                "Shared asset created successfully",
                sharedAssetService.create(request)
        );
    }

    @GetMapping("/shared-assets/{partnerOrgId}/deal/{dealId}")
    @Operation(
            summary = "Get Shared Assets",
            description = "Fetch all shared assets for a partner"
    )
    public SharkdomApiResponse<List<SharedAssetResponseDTO>> getAssets(
            @PathVariable Long partnerOrgId,
            @PathVariable String dealId) {

        log.info("API: Fetch Shared Assets | partnerOrgId={}", partnerOrgId);

        return new SharkdomApiResponse<>(
                true,
                "Shared assets fetched successfully",
                sharedAssetService.getAssets(partnerOrgId,dealId)
        );
    }

    @PostMapping("/activities")
    @Operation(
            summary = "Create Activity Log",
            description = "Create a new activity log entry for timeline"
    )
    public SharkdomApiResponse<String> createActivity(
            @RequestBody ActivityLogRequestDTO request) {

        log.info("API: Create Activity | partnerOrgId={}", request.getPartnerOrgId());

        activityLogService.createLog(request);

        return new SharkdomApiResponse<>(
                true,
                "Activity created successfully",
                "SUCCESS"
        );
    }

    @GetMapping("/activities/{partnerOrgId}/deal/{dealId}")
    @Operation(
            summary = "Get Activity Timeline",
            description = "Fetch activity timeline for a partner organization"
    )
    public SharkdomApiResponse<List<ActivityLogResponseDTO>> getActivities(
            @PathVariable Long partnerOrgId,
            @PathVariable String dealId) {

        log.info("API: Fetch Activities | partnerOrgId={}", partnerOrgId);

        return new SharkdomApiResponse<>(
                true,
                "Activities fetched successfully",
                activityLogService.getLogs(partnerOrgId,dealId)
        );
    }

    @PostMapping("/shared-contacts")
    @Operation(summary = "Create Shared Contact")
    public SharkdomApiResponse<SharedContactResponseDTO> createContact(
            @RequestBody SharedContactRequestDTO request) {

        log.info("API: Create SharedContact | name={}", request.getName());

        return new SharkdomApiResponse<>(
                true,
                "Contact created successfully",
                sharedContactService.create(request)
        );
    }

    @DeleteMapping("/shared-contacts/{id}")
    @Operation(
            summary = "Delete Shared Contact",
            description = "Soft delete a shared contact by ID"
    )
    public SharkdomApiResponse<String> deleteContact(
            @PathVariable Long id) {

        log.info("API: Delete SharedContact | id={}", id);

        sharedContactService.delete(id);

        return new SharkdomApiResponse<>(
                true,
                "Contact deleted successfully",
                "SUCCESS"
        );
    }

    @GetMapping("/shared-contacts/{partnerOrgId}/deal/{dealId}")
    @Operation(
            summary = "Get Shared Contacts by Deal ID",
            description = "Fetch shared contacts for a specific deal under a partner organization"
    )
    public SharkdomApiResponse<List<SharedContactResponseDTO>> getContactsByDealId(
            @PathVariable Long partnerOrgId,
            @PathVariable String dealId) {

        log.info("API: Fetch SharedContacts by dealId | partnerOrgId={} | dealId={}",
                partnerOrgId, dealId);

        return new SharkdomApiResponse<>(
                true,
                "Contacts fetched successfully",
                sharedContactService.getByDealId(partnerOrgId, dealId)
        );
    }

    @PutMapping("/shared-contacts/{id}")
    @Operation(summary = "Update Shared Contact")
    public SharkdomApiResponse<SharedContactResponseDTO> updateContact(
            @PathVariable Long id,
            @RequestBody SharedContactRequestDTO request) {

        log.info("API: Update SharedContact | id={}", id);

        return new SharkdomApiResponse<>(
                true,
                "Contact updated successfully",
                sharedContactService.update(id, request)
        );
    }

    @GetMapping("/shared-contacts/{partnerOrgId}/deals/{dealId}")
    @Operation(summary = "Get Shared Contacts")
    public SharkdomApiResponse<List<SharedContactResponseDTO>> getContacts(
            @PathVariable Long partnerOrgId,
            @PathVariable String dealId) {

        log.info("API: Fetch SharedContacts | partnerOrgId={}", partnerOrgId);

        return new SharkdomApiResponse<>(
                true,
                "Contacts fetched successfully",
                sharedContactService.get(partnerOrgId,dealId)
        );
    }

    @Operation(
            summary = "Create HubSpot Contact",
            description = "This API creates a new contact in HubSpot using the organizationId extracted from the authentication token. "
                    + "It accepts properties and optional associations in request body."
    )
    @PostMapping("/hubspot/contact/create")
    public SharkdomApiResponse<Map<Object, Object>> createHubspotContact(
            @RequestBody Map<String, Object> payload
    ) {

        return new SharkdomApiResponse<>(
                true,
                "HubSpot contact created successfully",
                hubspotService.createContact(payload)
        );
    }

    @GetMapping("/deal/owner/name")
    @Operation(
            summary = "Deal owner API for name and position",
            description = "Accepts dealId and returns a static backend response"
    )
    public SharkdomApiResponse<String> testDealApi(
            @RequestParam String dealId) {

        log.info("API: Test Deal | dealId={}", dealId);

        String response = "Ayush Shrivastava - backend";

        log.info("Returning static response for dealId={}", dealId);

        return new SharkdomApiResponse<>(
                true,
                "Response fetched successfully",
                response
        );
    }

    @PostMapping("/generate-intro")
    @Operation(
            summary = "Generate Introduction Email",
            description = "Accepts intro type and payload data, calls intro generation API and returns generated content"
    )
    public SharkdomApiResponse<IntroGenerateResponse> generateIntro(
            @RequestBody IntroGenerateRequest request) {

        log.info("API: Generate Intro | type={}", request.getType());

        IntroGenerateResponse response = introService.generateIntro(request);

        log.info("Successfully generated intro | type={}", request.getType());

        return new SharkdomApiResponse<>(
                true,
                "Intro generated successfully",
                response
        );
    }

    @GetMapping("/deal-owner-details/{dealId}")
    public ResponseEntity<DealOwnerDetailsResponseDto> getDealOwnerDetails(
            @RequestParam Long organizationId,
            @PathVariable String dealId
    ) {

        log.info(
                "REST request received for fetching deal owner details. organizationId: {}, dealId: {}",
                organizationId,
                dealId
        );

        DealOwnerDetailsResponseDto response =
                dealOwnerDetailsService.getDealOwnerDetails(
                        organizationId,
                        dealId
                );

        log.info(
                "REST request completed successfully for dealId: {}",
                dealId
        );

        return ResponseEntity.ok(response);
    }


    @PostMapping("/calculate")
    public ResponseEntity<CoSellHealthResponse> calculateCoSellHealthScore(
            @RequestBody CoSellHealthRequest request
    ) {

        log.info(
                "REST request received for calculating co-sell health score. dealStage: {}, stakeholderContacts: {}",
                request.getDealStage(),
                request.getStakeholderContacts()
        );

        CoSellHealthResponse response =
                coSellHealthScoreService.calculateHealthScore(request);

        log.info(
                "Co-sell health score calculated successfully. finalScore: {}, healthStatus: {}",
                response.getFinalScore(),
                response.getHealthStatus()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/request/intro/tracker/{partnerId}/deal/{dealId}")
    public ResponseEntity<PartnershipResponseMonitorDto> getPartnershipTracker(
            @PathVariable String dealId,
            @PathVariable Long partnerId
    ) {

        log.info(
                "REST request received for fetching partnership tracker details"
        );

        PartnershipResponseMonitorDto response =
                dealOwnerDetailsService.getStaticPartnershipTracker();

        log.info(
                "Partnership tracker details fetched successfully. currentStage: {}",
                response.getCurrentStage()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/find-employees")
    public ResponseEntity<DataEnrichmentResponse> findEmployees(
            @RequestBody DataEnrichmentRequest request
    ) {

        log.info(
                """
                        
                        REST request received for finding employees.
                        company: {}, designation: {}, location: {}, limit: {}
                        """,
                request.getCompany(),
                request.getDesignation(),
                request.getLocation(),
                request.getLimit()
        );

        DataEnrichmentResponse response =
                dataEnrichmentService.findEmployees(request);

        log.info(
                """
                        
                        Employees fetched successfully.
                        totalFetched: {}, totalMatched: {}, cached: {}
                        """,
                response.getTotalFetched(),
                response.getTotalMatched(),
                response.getCached()
        );

        return ResponseEntity.ok(response);
    }
}

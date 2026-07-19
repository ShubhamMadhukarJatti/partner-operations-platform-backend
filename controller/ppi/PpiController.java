package com.sharkdom.controller.ppi;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.ppi.FormType;
import com.sharkdom.dto.*;
import com.sharkdom.entity.ai.OverlapRecordFieldResponse;
import com.sharkdom.entity.ai.PersonaVersionResponse;
import com.sharkdom.entity.ai.PersonaVersioning;
import com.sharkdom.entity.ppi.*;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.model.ai.PersonaMode;
import com.sharkdom.model.ppi.*;

import com.sharkdom.repository.ppi.PartnerProgramDNSDataRepository;
import com.sharkdom.service.ai.PersonaVersioningService;
import com.sharkdom.service.ppi.*;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/ppi")
public class PpiController {

    @Autowired
    PpiService ppiService;

    @Autowired
    private OrganizationFormRequestService organizationFormRequestService;

    @Autowired
    private DnsUtil dnsUtil;

    @Autowired
    private DomainGenerationService domainGenerationService;

    @Autowired
    private AzureCdnService azureCdnService;

    @Autowired
    private PartnerProgramDNSDataRepository partnerProgramDNSDataRepository;

    @Autowired
    private PersonaVersioningService personaVersioningService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${azure.afd.endpoint}")
    private String afdEndpoint;

    @Value("${azure.afd.route-name}")
    private String routeName;

    @Value("${azure.afd.origin-group}")
    private String originGroup;

    @PostMapping("/create-project")
    public ResponseEntity<?> createScriptProject(@RequestBody CreateProjectRequest request) throws Exception {
        Object response = ppiService.createProject(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/create-project")
    public ResponseEntity<List<CreateProject>> getCreateScriptProject() throws Exception {
        List<CreateProject> response = ppiService.getCreateScriptProject();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update")
    public ScriptUpdateResponse updateScript(@RequestBody ScriptUpdateWrapper scriptUpdateWrapper) {
        ScriptUpdateResponse scriptUpdateResponse = new ScriptUpdateResponse();
        scriptUpdateResponse = ppiService.updateScript(scriptUpdateWrapper);
        return scriptUpdateResponse;

    }

    @PostMapping("/deployments")
    public DeploymentResponse createDeployment(@RequestBody DeploymentWrapper deploymentWrapper) {
        return ppiService.createDeployment(deploymentWrapper);
    }

    @PostMapping("/create-version")
    public ResponseEntity<CreateVersionResponse> createVersion(@RequestBody CreateVersionRequest request) {
        CreateVersionResponse response = ppiService.createScriptVersion(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/project-metadata")
    public ResponseEntity<GoogleDetailsResponse> getProjectMetadata(
            @RequestBody GoogleDetailsRequest request) {


        GoogleDetailsResponse response = ppiService.getProjectMetadata(request);
        return ResponseEntity.ok(response);

    }


    @PostMapping("/fetch-and-save")
    public ResponseEntity<String> fetchAndSaveResponderUrl(
            @RequestBody ScriptRequest scriptRequest) {
        try {
            Map<String, Object> response = ppiService.fetchAndSaveResponderUrl(scriptRequest);
            return ResponseEntity.ok("Responder URL saved successfully: " + response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + ex.getMessage());
        }
    }

    @PostMapping("/check-link")
    public FormLinkCheckResponse checkFormLinked(@RequestBody FormLinkCheckRequest request) {
        return ppiService.checkFormLink(request.getFormId(), request.getAccessToken());
    }

    @GetMapping("/list-triggers")
    public TriggerListResponse getTriggers(@RequestParam String accessToken) {

        return ppiService.listTriggers(accessToken);
    }

    @GetMapping("/fetchBy/organizationId")
    public ResponseEntity<PpiEntity> fetchByOrgId(@RequestParam Long organizationId) {
        return ResponseEntity.ok(ppiService.fetchByOrgId(organizationId));
    }

    @Operation(summary = "Save question form  Possible value for status ACTIVE,DRAFT,DELETE,ARCHIVE and Possible values for responseTypePpi  SINGLETEXT,MULTITEXT,MCQSINGLEOPTION,MCQMULTIOPTION and NUMERICRESPONSE")
    @PostMapping("question/internalForm/save")
    public ResponseEntity<QuestionResponse> saveQuestion(@RequestBody InternalQuestion_Ppi question) {
        QuestionResponse response = ppiService.saveQuestion(question);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Save response form Possible values for responseTypePpi  SINGLETEXT,MULTITEXT,MCQSINGLEOPTION,MCQMULTIOPTION,NUMERICRESPONSE")
    @PostMapping("response/internalForm/save")
    public ResponseEntity<QuestionResponse> saveResponse(@RequestBody BulkSaveResponseRequest request) {
        QuestionResponse response = ppiService.saveResponse(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fetchQuestionByOrgId")
    public ResponseEntity<List<InternalQuestion_Ppi>> fetchQuestionByOrgId() {
        return ResponseEntity.ok(ppiService.fetchQuestionByOrgId());
    }

    @GetMapping("/fetchResponseByOrgId")
    public ResponseEntity<List<InternalResponse_Ppi>> fetchResponseByOrgId() {
        return ResponseEntity.ok(ppiService.fetchResponseByOrgId());
    }

    @GetMapping("/stepping")
    public ResponseEntity<Map<String, Boolean>> fetchStepping() {
        return ResponseEntity.ok(ppiService.fetchStepping());
    }

    @PostMapping("/counter")
    public void incrementCounter(@RequestBody  CounterRequest counterRequest) {
        ppiService.incrementCounter(counterRequest);
    }

    @GetMapping("/getCounter")
    public ResponseEntity<CounterStatsResponse> fetchCounter() {
        CounterStatsResponse counters = ppiService.getOrgCounterStats();
        return ResponseEntity.ok(counters);
    }
    @GetMapping("/getCounterByFormId")
    public ResponseEntity<CounterStatsResponse> fetchCounterByFormId(@RequestParam String formId,@RequestParam Long orgId) {
        CounterStatsResponse counters = ppiService.getFormCounterStats(formId,orgId);
        return ResponseEntity.ok(counters);
    }


    @GetMapping("/getAllForms")
    public ResponseEntity<List<FormDetails>> getRecentEntries() {
        return ResponseEntity.ok(ppiService.getRecentEntries());
    }

    @PostMapping("/formView")
    public ResponseEntity<List<InternalFormResponse>> postInternalForm(@RequestBody InternalFormViewRequest request) {
        String formId = request.getFormId();
        String formType = request.getFormType();
        return ResponseEntity.ok(ppiService.postInternalFormV1(formId, formType));
    }

    @PostMapping("/enable-form")
    @Operation(
            summary = "Handle form status update or existence check",
            description = "Use operation = STATUS_UPDATE to enable/disable a form. Use operation = CHECK_EXISTENCE to verify if the opposite form type already exists (e.g., check for internal form if trying to activate a google form)."
    )
    public ResponseEntity<Map<String, Object>> enableForm(@RequestBody FormEnableRequest request) {
        Map<String, Object> response = ppiService.handleFormEnableRequest(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/findByResponseId")
    public ResponseEntity<ViewDetailsWrapperResponse> fetchByResponseId(
            @RequestParam(required = false) Long responseId,
            @RequestParam String formType,
            @RequestParam(required = false) List<Long> responseIdList) {

        return ResponseEntity.ok(ppiService.fetchByResponseId(responseId, formType, responseIdList));
    }

    @Operation(summary = "Possible values for Form Status: NOT_ASSIGNED, ASSIGNED, REJECTED, REVIEWED")
    @PatchMapping("/status/update")
    public ResponseEntity<LogResponse> updateStatus(@RequestBody StatusUpdateRequest request) {
        try {
            return ppiService.updateStatusRequest(request);
        } catch (ResponseStatusException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(createErrorLogResponse(e.getReason()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorLogResponse("An unexpected error occurred: " + e.getMessage()));
        }
    }

    @GetMapping("/fetchScript")
    public ResponseEntity<String> fetchScript() {
        return ResponseEntity.ok(ppiService.fetchScript());
    }

    private LogResponse createErrorLogResponse(String message) {
        LogResponse error = new LogResponse();
        error.setUpdatedFormStatus("FAILED");
        error.setModifiedBy(message);
        error.setModifiedDate(new Date());
        return error;
    }

    @Operation(summary = "Possible values for Form Status: ACTIVE, DRAFT, DELETE, ARCHIVE")
    @PatchMapping("/question/update")
    public ResponseEntity<QuestionResponse> patchQuestion(@RequestBody InternalQuestion_Ppi request) {
        QuestionResponse response = ppiService.patchQuestionById(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save/Script")
    public ResponseEntity<ScriptDetails> saveScript(@RequestBody ScriptRequest request) {
        ScriptDetails response = ppiService.saveScript(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fetchScriptByOrgId")
    public ResponseEntity<List<ScriptDetails>> fetchScriptByOrgId() {
        return ResponseEntity.ok(ppiService.fetchScriptByOrgId());
    }


    @GetMapping("/fetch/webhook/question-response")
    public ResponseEntity<?> fetchwebhookDetails() {
        try {
            List<WebHookQuestionResponse> responses = ppiService.fetchwebhookDetails();

            if (responses == null || responses.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(responses);
        } catch (Exception e) {

            log.info("Exception in fetchwebhookDetails: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching webhook details: " + e.getMessage());
        }
    }

    @GetMapping("/fetchQuestionByFormId")
    public ResponseEntity<?> getQuestions(
            @RequestParam String formId,
            @RequestParam FormType formType) {
        try {
            List<?> questions = ppiService.getQuestionsByFormIdAndType(formId, formType);
            return ResponseEntity.ok(questions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/no/auth/fetchQuestionByFormId")
    public ResponseEntity<?> getQuestionsNoAuth(
            @RequestParam String formId,
            @RequestParam FormType formType) {
        try {
            List<?> questions = ppiService.
                    getQuestionsByFormIdAndTypeNOAuth(formId, formType);
            return ResponseEntity.ok(questions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/is-applied")
    public ResponseEntity<Map<String, Boolean>> getIsApplied(@RequestParam Long formId,
                                                             @RequestParam Long appliedOrgId) {
        Boolean isApplied = ppiService.getIsApplied(formId, appliedOrgId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isApplied", isApplied != null && isApplied);

        if (isApplied != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }

    @PutMapping("/branding")
    public SharkdomApiResponse<PartnerPortalBrandingResponse>
    upsertBranding(
            @RequestBody PartnerPortalBrandingRequest request
    ) {
        return ppiService.upsertBranding(request);
    }

    @GetMapping("/org/branding")
    public SharkdomApiResponse<PartnerPortalBrandingResponse> getBrandingByOrg() {
        var orgId = Util.getOrgIdFromToken();
        return ppiService.getBrandingByOrgId(orgId);
    }

    @GetMapping("/org/branding/exists")
    public SharkdomApiResponse<Boolean> checkBrandingByOrg() {

        Long orgId = Util.getOrgIdFromToken();
        return ppiService.checkBrandingCreatedForOrg(orgId);
    }

    @GetMapping("/checking-dns")
    public ResponseEntity<Map<String, Object>> checkDns(
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String target
    ) {
        Map<String, Object> response = new HashMap<>();
        // Validation (same as PHP)
        if (domain == null || domain.isBlank() || target == null || target.isBlank()) {
            response.put("success", false);
            response.put("error", "domain and target are required");
            return ResponseEntity.badRequest().body(response);
        }
        boolean result = DnsUtil.checkCname(domain, target);
        response.put("success", true);
        response.put("domain", domain);
        response.put("target", target);
        response.put("dns_detected", result);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/org-form-requests")
    public ResponseEntity<SharkdomApiResponse<OrganizationFormRequest>> createFormRequest(
            @RequestBody OrganizationFormRequestCreateDto request
    ) {
        OrganizationFormRequest saved =
                organizationFormRequestService.createFormRequestV1(request);
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Organization form request created successfully",
                        saved
                )
        );
    }

    @GetMapping("/form/status")
    public ResponseEntity<SharkdomApiResponse<FormStatusResponseDto>> getFormStatus(
            @RequestParam String formId,
            @RequestParam String senderEmail
    ) {
        FormStatusResponseDto response =
               organizationFormRequestService.getCurrentStatusWithTime(formId, senderEmail);
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Form status fetched successfully",
                        response
                )
        );
    }

    @PostMapping("/save/dns")
    public ResponseEntity<SharkdomApiResponse<PartnerProgramDNSData>> createDNSData(
            @RequestBody PartnerProgramDNSCreateRequest request
    ) {
        PartnerProgramDNSData saved =
                dnsUtil.createDNSData(request);
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Partner program DNS data created successfully",
                        saved
                )
        );
    }

    @PostMapping("/generate/target-host")
    public ResponseEntity<SharkdomApiResponse<GeneratedDomainResponse>> generateTargetHost(
            @RequestBody GenerateTargetHostRequest request
    ) {
        GeneratedDomainResponse response =
                domainGenerationService.generateTargetHost(request.getDomainName());
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Target host generated successfully",
                        response
                )
        );
    }

    @PostMapping("/verify-and-save-dns")
    public ResponseEntity<SharkdomApiResponse<PartnerProgramDNSData>> verifyAndSaveDns(
            @RequestBody PartnerProgramDNSVerifyRequest request
    ) {
        PartnerProgramDNSData saved =
                dnsUtil.verifyAndSaveDns(request);
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "DNS verified and data saved successfully",
                        saved
                )
        );
    }

    @GetMapping("/dns/connection-status")
    public ResponseEntity<SharkdomApiResponse<DnsConnectionStatusResponse>> checkDnsConnectionStatus() {

        var dnsConnectionStatusResponse = dnsUtil.checkDnsConnectionStatusWithTxtValidation();

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "DNS connection status fetched successfully",
                        dnsConnectionStatusResponse
                )
        );
    }

    @Operation(summary = "Save response form Possible values for responseTypePpi  SINGLETEXT,MULTITEXT,MCQSINGLEOPTION,MCQMULTIOPTION,NUMERICRESPONSE")
    @PostMapping("/response/internalForm/external/save")
    public ResponseEntity<QuestionResponse> saveResponseForExternal(@RequestBody BulkSaveResponseRequestForExternal request) {
        QuestionResponse response = ppiService.saveResponseForExternalForm(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dns/by-org")
    public ResponseEntity<SharkdomApiResponse<PartnerProgramDNSData>> getDnsByOrgId(
    ) {
        var orgId = Util.getOrgIdFromToken();
        PartnerProgramDNSData data =
                dnsUtil.getDnsDataByOrganizationId(orgId);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "DNS data fetched successfully",
                        data
                )
        );
    }

    @Operation(
            summary = "Get Azure CDN custom domain status",
            description = "Fetches validation and provisioning status of a custom domain from Azure CDN"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Custom domain details fetched successfully",
            content = @Content(schema = @Schema(implementation = AzureCustomDomainStatusResponse.class))
    )
    @GetMapping(
            value = "/custom-domain/{customDomainName}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SharkdomApiResponse<AzureCustomDomainStatusResponse>> getCustomDomainDetails(
            @PathVariable @NotBlank(message = "Custom domain name is required")
            String customDomainName
    ) {
        long startTime = System.currentTimeMillis();
        log.info("API Call → Get Custom Domain Status | Domain: {}", customDomainName);

        AzureCustomDomainStatusResponse response =
                azureCdnService.getCustomDomainDetails(customDomainName);

        log.info("API Success → Status fetched in {} ms",
                System.currentTimeMillis() - startTime);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Custom domain details fetched successfully",
                        response
                )
        );
    }


    @GetMapping("/dns/domain-validation-status/by-org")
    public ResponseEntity<SharkdomApiResponse<Map<String, String>>> getDomainValidationStatusByOrg() {
        Map<String, String> response = dnsUtil.getDomainValidationStatusByOrgId();
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Domain validation status fetched successfully",
                        response
                )
        );
    }

    @PutMapping("/stepper")
    public SharkdomApiResponse<PartnerProgramStepper>
    upsertStepper(@RequestBody PartnerProgramStepperRequest request) {
        return ppiService.upsertStepper(request);
    }

    @GetMapping("/get/stepper")
    public SharkdomApiResponse<PartnerProgramStepper> getStepper() {
        log.info("GET /partner-program/stepper called");
        return ppiService.getStepper();
    }


    @GetMapping("/dns/txt-record/by-org")
    public ResponseEntity<SharkdomApiResponse<DnsInstruction>> getTxtRecordByOrg() {

        long startTime = System.currentTimeMillis();
        Long organizationId =Util.getOrgIdFromToken();

        log.info("TXT record fetch started for organizationId={}", organizationId);

        try {
            // Step 1: Fetch DNS data for org
            PartnerProgramDNSData dnsData =
                    dnsUtil.getDnsDataByOrganizationId(organizationId);

            if (dnsData.getCustomDomain() == null || dnsData.getCustomDomain().isBlank()) {
                log.warn("Custom domain missing for organizationId={}", organizationId);
                throw new ServiceException(ErrorMessages.SH165);
            }

            String customDomain = dnsData.getCustomDomain();
            log.info("Fetching TXT record for customDomain={}", customDomain);

            // Step 2: Call service
            DnsInstruction dnsInstruction =
                    dnsUtil.getTxtRecord(customDomain);

            // Step 3: Handle PENDING state
            if (dnsInstruction == null) {
                log.info("DNS validation token still pending for customDomain={}", customDomain);

                return ResponseEntity.ok(
                        new SharkdomApiResponse<>(
                                false,
                                "Domain validation is IN PENDING state. Please click again after some time.",
                                null
                        )
                );
            }

            dnsData.setRecordType("TXT");
            dnsData.setValidationToken(dnsInstruction.getValue());
            dnsData.setTxtRecord(dnsInstruction.getName());
            partnerProgramDNSDataRepository.save(dnsData);
            log.info("TXT record fetched successfully for customDomain={}", customDomain);

            return ResponseEntity.ok(
                    new SharkdomApiResponse<>(
                            true,
                            "TXT record fetched successfully",
                            dnsInstruction
                    )
            );

        } catch (ServiceException se) {
            log.error("Service exception while fetching TXT record for orgId={}: {}",
                    organizationId, se.getMessage());
            throw se;
        } catch (Exception ex) {
            log.error("Unexpected error while fetching TXT record for orgId={}",
                    organizationId, ex);
            throw new ServiceException(
                    ErrorMessages.SH171,
                    organizationId
            );
        } finally {
            log.info("TXT record fetch completed for organizationId={} in {} ms",
                    organizationId,
                    System.currentTimeMillis() - startTime);
        }
    }

    @PutMapping("/routes/custom-domain")
    public ResponseEntity<SharkdomApiResponse<String>> updateRouteWithCustomDomain(
            @RequestParam String customDomainName
    ) {

        long startTime = System.currentTimeMillis();
        log.info("Update AFD route started for customDomainName={}", customDomainName);

        try {
            // Step 1: Validate input
            if (!StringUtils.hasText(customDomainName)) {
                log.warn("Invalid customDomainName received");
                throw new ServiceException(ErrorMessages.SH165);
            }


            azureCdnService.updateRouteWithCustomDomainA1(afdEndpoint,routeName,customDomainName);

            String message = String.format(
                    "Route updated successfully with custom domain: %s",
                    customDomainName
            );

            log.info("Update AFD route successful for customDomainName={} in {} ms",
                    customDomainName,
                    System.currentTimeMillis() - startTime
            );

            // Step 3: Success response
            return ResponseEntity.ok(
                    new SharkdomApiResponse<>(
                            true,
                            message,
                            customDomainName
                    )
            );

        } catch (ServiceException se) {
            log.error("Service exception while updating route for domain={}: {}",
                    customDomainName, se.getMessage());
            throw se;
        } catch (Exception ex) {
            log.error("Unexpected error while updating route for domain={}",
                    customDomainName, ex);
            throw new ServiceException(
                    ErrorMessages.SH171,
                    customDomainName
            );
        } finally {
            log.info("Update AFD route completed for customDomainName={} in {} ms",
                    customDomainName,
                    System.currentTimeMillis() - startTime
            );
        }
    }

    @PostMapping("/counter/external/increment")
    public void incrementCounterExternal(@RequestBody  ExternalUserCounterRequest counterRequest) {
        ppiService.incrementCounterExternalUser(counterRequest);
    }

    @GetMapping("/org/branding/external/user/{orgId}")
    public ResponseEntity<SharkdomApiResponse<PartnerPortalBrandingResponse>>
    getBrandingByOrgForExternalUser(@PathVariable("orgId") Long orgId) {
        return ResponseEntity.ok(
                ppiService.getBrandingByOrgId(orgId)
        );
    }

    @GetMapping("/persona/version/data")
    public ResponseEntity<PersonaVersionResponse> getVersion(
            @RequestParam Long orgId,
            @RequestParam PersonaMode personaMode,
            @RequestParam Integer version
    ) {


        var versionByNumber = personaVersioningService.getVersionByNumber(
                orgId,
                personaMode,
                version
        );

        return ResponseEntity.ok(versionByNumber);
    }

    @GetMapping("/persona/versions")
    public ResponseEntity<List<PersonaVersionResponse>> getAllVersions(
            @RequestParam(required = false) PersonaMode personaMode
    ) {
        var orgId = Util.getOrgIdFromToken();
        var response =
                personaVersioningService
                        .getAllVersionsByOrgId(orgId, personaMode);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/persona/version/fields")
    public ResponseEntity<List<OverlapRecordFieldResponse>> getFields(
            @RequestParam PersonaMode personaMode,
            @RequestParam Integer version
    ) {
        var orgId = Util.getOrgIdFromToken();
        var response = personaVersioningService
                .getOverlapFieldsFromVersion(orgId, personaMode, version);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/org/dns/azure/{azureDomainResourceName}")
    public ResponseEntity<SharkdomApiResponse<PartnerProgramDNSData>>
    getDNSDataByAzureDomainResourceName(
            @PathVariable String azureDomainResourceName) {

        return ResponseEntity.ok(
                ppiService.getDNSDataByAzureResourceName(azureDomainResourceName)
        );
    }


    @GetMapping("/PartnerDnsData")
    public List<PartnerProgramDNSData> getAllPartnerDNSDATA() {
        return dnsUtil.getAllDnsData();
    }


}
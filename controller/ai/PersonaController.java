package com.sharkdom.controller.ai;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.dto.ResetIntegrationRequestDTO;
import com.sharkdom.entity.ai.*;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.model.PersonaResponse;
import com.sharkdom.model.PersonaStatus;
import com.sharkdom.model.ai.*;
import com.sharkdom.model.persona.PersonaMatchDto;
import com.sharkdom.repository.ai.PersonaRepository;
import com.sharkdom.repository.ai.PersonaStatusRepository;
import com.sharkdom.service.ai.*;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController

@CrossOrigin
@Slf4j
@RequestMapping("/persona")
public class  PersonaController {

    private final PersonaService personaService;
    private final PersonaRepository personaRepository;
    private final PersonaStatusRepository personaStatusRepository;
    private final AsyncPersonaService asyncPersonaService;
    private final IntegrationResetService integrationResetService;
    private final PersonaOverlappingVersion personaOverlappingVersion;
    private final ProspectPersonaService prospectPersonaService;
    private final CustomerPersonaService customerPersonaService;
    private final OpportunityPersonaService opportunityPersonaService;

    public PersonaController(PersonaService personaService, PersonaRepository personaRepository, PersonaStatusRepository personaStatusRepository, AsyncPersonaService asyncPersonaService, IntegrationResetService integrationResetService, PersonaOverlappingVersion personaOverlappingVersion, ProspectPersonaService prospectPersonaService, CustomerPersonaService customerPersonaService, OpportunityPersonaService opportunityPersonaService) {
        this.personaService = personaService;
        this.personaRepository = personaRepository;
        this.personaStatusRepository = personaStatusRepository;
        this.asyncPersonaService = asyncPersonaService;
        this.integrationResetService = integrationResetService;
        this.personaOverlappingVersion = personaOverlappingVersion;
        this.prospectPersonaService = prospectPersonaService;
        this.customerPersonaService = customerPersonaService;
        this.opportunityPersonaService = opportunityPersonaService;
    }

    @Operation(summary = "Persona Controller")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(path = "")
    public Map<String, String> savePersona(@RequestBody PersonaRequest personaRequest) {
        log.info("Request received to save persona for organization ID: {}", personaRequest.getOrganizationId());
        var res = personaRepository.getAllByOrganizationId(personaRequest.getOrganizationId());
        if (res.size() >= 2) {
            throw new SharkdomException(ErrorMessages.SH85);
        }
        var status = personaStatusRepository.getByOrganizationId(personaRequest.getOrganizationId());
        if (status == null) {
            var entity = PersonaStatusEntity.builder()
                    .columnName(personaRequest.getColumnName())
                    .googleSheetLink(personaRequest.getGoogleSheetLink())
                    .personaMode(personaRequest.getPersonaMode())
                    .fileName(personaRequest.getFileName())
                    .frequency(personaRequest.getFrequency())
                    .personaStatus(PersonaStatus.INITIATED)
                    .organizationId(personaRequest.getOrganizationId())
                    .build();
            personaStatusRepository.save(entity);
        }
//        asyncPersonaService.savePersona(personaRequest);
        return Map.of("response", "request submitted");
    }

    @Operation(summary = "Persona Controller")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/details")
    public PersonaResponse getPersonaDetails(@RequestParam(value = "page", defaultValue = "0") int page,
                                             @RequestParam(value = "size", defaultValue = "20") int size) {
        return personaService.getPersonaDetails(page, size);
    }

    @Operation(summary = "Persona Controller")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/v1/details/{id}")
    public PersonaResponse getPersonaDetailsV1(@RequestParam(value = "page", defaultValue = "0") int page,
                                             @RequestParam(value = "size", defaultValue = "20") int size,
                                               @PathVariable Integer id) {
        return personaService.getPersonaDetailsV1(page, size,id);
    }

    @Operation(summary = "Persona Controller")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(path = "/details")
    public Map<String, String> saveDummy(@RequestBody CompanyDetails companyDetails) {
        return personaService.saveDummy(companyDetails);
    }

    @Operation(summary = "Notify")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notified successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(path = "/notify")
    public Map<String, String> notifyPersona(@RequestParam Long senderId, @RequestParam Long notifyId) {
        personaService.notifyPersona(senderId, notifyId);
        return Map.of("message", "organization notified successfully");
    }


    @Operation(summary = "Notify users of other org to create persona")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notified successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(path = "/get-notified/{senderOrgID}")
    public Map<String, String> notifyCurrentPersona(@PathVariable String senderOrgID) {
        if(personaService.saveOrUpdateNotify(Util.getOrgIdFromToken(), Long.valueOf(senderOrgID))){
            return Map.of("message", "organization notified successfully");
        }
        else {
            return Map.of("message", "No persona found");
        }
    }

    @Operation(summary = "GET Notify Details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notified successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/notify")
    public List<PersonaNotifyEntity> getPersonaNotifyDetails() {
        return personaService.notifyPersonaDetails();
    }

    @Operation(summary = "Save Persona Mode")
    @PostMapping(path = "/mode")
    public Map<String, String> getPersonaNotifyDetails(@RequestBody ModeSaveRequest modeSaveRequest) {
        return personaService.saveModeDetails(modeSaveRequest);
    }

    @PostMapping("/overlap")
    public OverlapRecordEntity saveOverlapRecords(@RequestBody OverlapRecordsRequest overlapRecords) {
        log.info("overlap method start", overlapRecords);
        return personaService.saveOverlapRecords(overlapRecords);
    }

    @GetMapping("/overlap")
    public Map<String, Integer> getOverlapRecords(@RequestParam Long partnerId) {
        return personaService.getOverlapRecords(partnerId);
    }

    @GetMapping("/overlap/my-records")
    @Operation(summary = "Get paginated overlap records for an organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Records retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})
    public List<OverlapRecordEntity> getOverlapRecords(
            @RequestParam(required = false) RecordType recordType) {
        return personaService.getOverlapRecords(recordType);
    }

    @DeleteMapping("/overlap/my-records")
    @Operation(summary = "Delete overlap records and related persona data for an organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Records deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})
    public ResponseEntity<Map<String, String>> deleteOverlapRecords(
            @RequestParam(required = false) RecordType recordType) {
        personaService.deleteOverlapRecords(recordType);
        return ResponseEntity.ok(Map.of("message", "Records deleted successfully"));
    }

    @PostMapping("/partner/permissions")
    @Operation(summary = "Set partner data access permissions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissions set successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})
    public PartnerDataPermissionEntity setPartnerPermissions(@RequestBody PartnerDataPermission request) {
        return personaService.setPartnerPermissions(request);
    }

    @GetMapping("/partner/data")
    @Operation(summary = "Get partner data based on permissions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})
    public Map<String, Object> getPartnerData(
            @RequestParam Long partnerId,
            @RequestParam(required = false) String type) {
        return personaService.getPartnerDataWithPermissions(partnerId, type);
    }

    @GetMapping("/partner-data-permissions/{organizationId}")
    public ResponseEntity<PartnerDataPermissionResponse> getPartnerDataPermissions(@PathVariable Long organizationId) {
        return ResponseEntity.ok(personaService.getPartnerDataPermissions(organizationId));
    }


    @Operation(summary = "Get persona match details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})
    @GetMapping("/persona-match/{organizationId}")
    public ResponseEntity<PersonaMatchDto> getPersonaMatch(@PathVariable Long organizationId) {
        return ResponseEntity.ok(personaService.getPersonaMatch(organizationId));
    }

    @Operation(summary = "Remove Integration")
    @DeleteMapping("/disconnect")
    public Map<String, String> disconnect() {
        personaService.removeLatestPersonaAndIntegration(Util.getOrgIdFromToken());
        return Map.of("message", "Integration deleted successfully");
    }

    @DeleteMapping("/overlap/my-records/delete")
    @Operation(summary = "Delete overlap records and related persona data for an organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Records deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})
    public ResponseEntity<Map<String, String>> deleteOverlapRecord(
            @RequestParam(required = false) RecordType recordType) {
        personaService.deleteOverlapRecord(recordType);
        return ResponseEntity.ok(Map.of("message", "Records deleted successfully"));
    }

    @Operation(summary = "Remove Integration")
    @DeleteMapping("/disconnect/CRM")
    public Map<String, String> disconnectCRM() {
        personaService.removeLatestPersonaAndIntegration(Util.getOrgIdFromToken());
        return Map.of("message", "Integration deleted successfully");
    }

    @GetMapping("/overlap/my-records/org/{organizationId}")
    @Operation(summary = "Get paginated overlap records for an organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Records retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})
    public List<OverlapRecordEntity> getOverlapRecordsForOrg(
            @PathVariable Long organizationId,
            @RequestParam(required = false) RecordType recordType) {
        return personaService.getOverlapRecord(recordType,organizationId);
    }

    @GetMapping("/partner/data/{userId}")
    @Operation(summary = "Get partner data based on permissions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})
    public Map<String, Object> getPartnerDataEPS(
            @PathVariable String userId,
            @RequestParam Long partnerId,
            @RequestParam(required = false) String type) {
        return personaService.getPartnerData(userId,partnerId);
    }

    @PostMapping("/crm/disconnect")
    @Operation(summary = "Reset integration and overlap frequency",
            description = "Clears refresh token, disconnects integration, and sets overlap frequency to NONE for latest record"
    )
    public SharkdomApiResponse<?> resetIntegration(
            @RequestBody ResetIntegrationRequestDTO requestDTO) {
        log.info("API request received: Reset integration for type={}", requestDTO.getIntegrationType());
        // Validation
        if (requestDTO.getIntegrationType() == null) {
            log.error("IntegrationType is null in request");
            throw new RuntimeException("IntegrationType must not be null");
        }
        integrationResetService.resetIntegrationAndFrequency(requestDTO.getIntegrationType(),requestDTO.getRecordType());
        log.info("Integration reset completed successfully for type={}", requestDTO.getIntegrationType());
        return new SharkdomApiResponse<>(
                true,
                "Integration and frequency reset successfully",
                null
        );
    }

    @Operation(summary = "Get overlap record versions by organization ID")
    @GetMapping("/overlap-records/{orgId}/versions")
    public ResponseEntity<SharkdomApiResponse<List<OverlapRecordVersionResponse>>> getOverlapVersions(
            @PathVariable Long orgId,
            @RequestParam(required = false) RecordType recordType) {

        log.info("[GET_OVERLAP_RECORD_VERSIONS_API] orgId={}", orgId);

        List<OverlapRecordVersionResponse> response =
                personaService.getOverlapVersionsByOrgId(orgId,recordType);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Overlap record versions fetched successfully",
                        response
                )
        );
    }

    @GetMapping("/overlap/v2/my-records")
    @Operation(summary = "Get overlap records by recordType and versionId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Records retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    public List<OverlapRecordEntity> getOverlapRecordsV2(
            @RequestParam(required = false) RecordType recordType,
            @RequestParam(required = false) Integer versionId) {

        return personaService.getOverlapRecordsV2(recordType, versionId);
    }

    @PostMapping("/overlap/manual-version")
    public ResponseEntity<String> triggerManualVersion() {

        personaOverlappingVersion.triggerManualVersionFromToken();

        return ResponseEntity.ok("Manual overlap version triggered successfully");
    }


    @Operation(summary = "Save Prospects overlap records.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Records saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/overlap/prospects")
    public OverlapRecordEntity saveOverlapProspectsRecords(@RequestBody OverlapRecordsRequest overlapRecords) {
        log.info("overlap method start", overlapRecords);
        return prospectPersonaService.saveOverlapRecordsForProspect(overlapRecords);
    }

    @Operation(summary = "Save Customer overlap records.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Records saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/overlap/customer")
    public OverlapRecordEntity saveOverlapCustomerRecords(@RequestBody OverlapRecordsRequest overlapRecords) {
        log.info("overlap method start", overlapRecords);
        return customerPersonaService.saveOverlapRecordsForCustomer(overlapRecords);
    }


    @Operation(summary = "Save Opportunity overlap records.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Records saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/overlap/opportunity")
    public OverlapRecordEntity saveOverlapOpportunityRecords(@RequestBody OverlapRecordsRequest overlapRecords) {
        log.info("overlap method start", overlapRecords);
        return opportunityPersonaService.saveOverlapRecordsForOpportunity(overlapRecords);
    }
}
package com.sharkdom.controller.noauth;

import com.sharkdom.constants.SwaggerConstants;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.dto.OverlapRecordsRequestnew;
import com.sharkdom.entity.ai.ExternalPartnerOverlapRecordEntity;
import com.sharkdom.entity.ai.ExternalPartnerOverlapRecordFieldEntity;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.entity.partnermapping.MyPartnerMappingReportStatus;
import com.sharkdom.integration.model.IntegrationSaveRequest;
import com.sharkdom.model.ai.OverlapRecordsRequest;
import com.sharkdom.model.ai.RecordType;
import com.sharkdom.model.meetings.CreateMeetingModel;
import com.sharkdom.pipedrive.dto.CreatePersonRequest;
import com.sharkdom.pipedrive.dto.CreatePersonResponse;
import com.sharkdom.pipedrive.dto.PersonFieldsResponse;
import com.sharkdom.pipedrive.dto.PersonsResponse;
import com.sharkdom.pipedrive.service.PipeDriveService;
import com.sharkdom.salesforce.dto.SalesforceDescribeResponse;
import com.sharkdom.salesforce.dto.SalesforceQueryResponse;
import com.sharkdom.salesforce.service.SalesforceService;
import com.sharkdom.service.ai.HubspotService;
import com.sharkdom.service.ai.IntegrationService;
import com.sharkdom.service.ai.PersonaService;
import com.sharkdom.service.ai.ZohoService;
import com.sharkdom.service.organization.OrganizationService;
import com.sharkdom.service.partnermapping.PartnerMappingService;
import com.sharkdom.util.SharkdomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/no/auth")
@RequiredArgsConstructor
public class EPSRestController {

    private final OrganizationService organizationService;
    private final PersonaService personaService;
    private final PartnerMappingService partnerMappingService;
    private final HubspotService hubspotService;
    private final IntegrationService integrationService;
    private final ZohoService zohoServiceAi;
    private final SalesforceService salesforceService;
    private final PipeDriveService pipeDriveService;


    @Operation(summary = "Save integration details")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service responded OK", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = IntegrationDetails.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(value = "/organization/integration")
    public IntegrationDetails saveIntegrationDetailsNoAuth(@Valid @RequestBody IntegrationSaveRequest integrationDetails) {
        return organizationService.saveIntegrationDetailsNoAuth(integrationDetails);
    }

    @Operation(summary = "Update integration details", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = CreateMeetingModel.class),
            examples = @ExampleObject(value = SwaggerConstants.PATCH_INTEGRATION))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service responded OK", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = IntegrationDetails.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PatchMapping(value = "/organization/integration")
    public IntegrationDetails updateIntegrationDetails(@RequestBody IntegrationDetails integrationDetails) {
        return organizationService.updateIntegrationNoAuth(integrationDetails);
    }

    @Operation(summary = "Get integration details by userId")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service responded OK", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = IntegrationDetails.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(value = "/organization/integration/{userId}")
    public List<IntegrationDetails> getIntegrationDetails(
            @PathVariable String userId
    ) {
        return organizationService.getIntegrationDetailsNoAuth(userId);
    }

    @GetMapping("/overlap/my/records/{userId}")
    @Operation(summary = "Get paginated overlap records for an organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Records retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})
    public List<ExternalPartnerOverlapRecordEntity> getOverlapRecords(
            @RequestParam(required = false) RecordType recordType,
            @PathVariable String userId
            ) {
        return personaService.getOverlapRecordsForUser(userId, recordType);
    }

    @PostMapping("/save/overlap")
    @Operation(summary = "Save overlap records for an organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Records retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})
    public ExternalPartnerOverlapRecordEntity saveOverlapRecords(@RequestBody OverlapRecordsRequest overlapRecords) {
        return personaService.saveOverlapRecordsForUser(overlapRecords);
    }

    @PostMapping("/report/save")
    public ResponseEntity<String> savePartnerMappingReport(@RequestBody MyPartnerMappingReportStatus reportStatus) {
        try {
            JSONObject response = partnerMappingService.savePartnerMappingReportForUser(reportStatus);
            return ResponseEntity.ok(response.toString());
        } catch (Exception ex) {
            // Proper error logging
            log.error("Error while saving partner mapping report", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to save partner mapping report\"}");
        }
    }

    @GetMapping("/report/history/{userId}")
    public ResponseEntity<String> getPartnerMappingReportStatus(
            @PathVariable String userId
    ) {
        try {
            JSONObject response = partnerMappingService.getPartnerMappingReportStatusForUser(userId);
            return ResponseEntity.ok(response.toString());
        } catch (Exception ex) {
            // Proper error logging
            log.error("Error while fetching partner mapping report status", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Failed to fetch partner mapping report status\"}");
        }
    }

    @PostMapping("/find-overlaps")
    public ResponseEntity<List<ExternalPartnerOverlapRecordFieldEntity>> getOverlappingRecords(
            @RequestBody OverlapRecordsRequestnew request
    ) {

        List<ExternalPartnerOverlapRecordFieldEntity> result =
                personaService.getOverlappingRecordsForUser(
                        request.getListA(),
                        request.getListB()
                );

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/overlap/my-records/{userId}")
    @Operation(summary = "Delete overlap records and related persona data for an organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Records deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})
    public ResponseEntity<Map<String, String>> deleteOverlapRecords(
            @RequestParam(required = false) RecordType recordType,
            @PathVariable String userId) {
        personaService.deleteOverlapRecordsForUser(recordType,userId);
        return ResponseEntity.ok(Map.of("message", "Records deleted successfully"));
    }

    @GetMapping("/report-count/{userId}")
    public ResponseEntity<SharkdomApiResponse<Long>> getReportGenerateCountForUser(
            @PathVariable String userId
    ) {

        Long count = partnerMappingService.getReportGenerateCountForUser(userId);
        SharkdomApiResponse<Long> response =
                new SharkdomApiResponse<>(
                        true,
                        "Report generate count fetched successfully",
                        count
                );
        return ResponseEntity.ok(response);
    }


    @PostMapping("/overlap/counts")
    public ResponseEntity<SharkdomApiResponse<Integer>> countOverlapUsers(
            @RequestBody OverlapRecordsRequestnew request
    ) {
        int count = personaService.countOverlapUser(
                request.getListA(),
                request.getListB()
        );
        SharkdomApiResponse<Integer> response =
                new SharkdomApiResponse<>(
                        true,
                        "Overlap count fetched successfully",
                        count
                );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Hubspot details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/hubspot")
    public Map<Object, Object> getPersonaDetails(@RequestParam String userId, @RequestParam String fields) {
        return hubspotService.getDetailsByUserId(userId, fields);
    }

    @Operation(summary = "Hubspot Fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/hubspot/fields/{userId}")
    public List<String> getHubspotFields(
            @PathVariable String userId
    ) {
        return hubspotService.getContactsByUserId(userId);
    }

    @Operation(summary = "Zoho Fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/zoho/fields/{userId}")
    public List<String> getZohoFields(String userId) throws URISyntaxException {
        return zohoServiceAi.getFieldsByUserId(userId);
    }

    @Operation(summary = "Zoho Data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/zoho/data/{userId}")
    public List<Map<String, Object>> getZohoData(@PathVariable String userId) throws URISyntaxException {
        return zohoServiceAi.getDataByUserId(userId);
    }

    @Operation(summary = "Get Salesforce Contacts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched contacts successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @GetMapping("/salesforce/contacts/{userId}")
    public ResponseEntity<?> getContacts(@RequestParam List<String> fields, @PathVariable String userId) {
        try {
            SalesforceQueryResponse response = salesforceService.getContactsByUserId(userId,fields);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Failed to fetch contacts: " + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Get Salesforce Contact Object Description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched description successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @GetMapping("/salesforce/contacts/describe/{userId}")
    public ResponseEntity<?> getDescription(@PathVariable String userId) {
        try {
            SalesforceDescribeResponse response = salesforceService.getDescriptionByUserId(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Failed to fetch description: " + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "pipedrive Fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/pipedrive/fields/{userId}")
    public ResponseEntity<PersonFieldsResponse> getPipedriveFields(String userId) throws URISyntaxException {
        return ResponseEntity.ok(pipeDriveService.getContactsByUserId(userId));
    }

    @Operation(summary = "PipeDrive Data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/pipedrive/data/{userId}")
    public ResponseEntity<PersonsResponse> getPipeDriveData(String userId) throws URISyntaxException {
        return ResponseEntity.ok(pipeDriveService.getDetailsByUserId(userId));
    }

    @Operation(summary = "PipeDrive Person Creation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Creation successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/pipedrive/create/person")
    public ResponseEntity<CreatePersonResponse> createPerson(@RequestBody CreatePersonRequest createPersonRequest) {
        CreatePersonResponse response = pipeDriveService.createPersonByUser(createPersonRequest);
        return ResponseEntity.ok(response);
    }
}

package com.sharkdom.datapipeline.controller;

import com.sharkdom.datapipeline.dto.HubspotMetadataRequest;
import com.sharkdom.datapipeline.service.DataPipeLineService;
import com.sharkdom.entity.ai.OverlapRecordEntity;
import com.sharkdom.model.ai.OverlapRecordsRequest;
import com.sharkdom.model.ai.RecordType;
import com.sharkdom.service.ai.HubspotService;
import com.sharkdom.util.SharkdomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Data Pipeline APIs.
 *
 * <p>
 * This controller handles ingestion of overlap records for:
 * <ul>
 *     <li>Opportunity</li>
 *     <li>Customer</li>
 *     <li>Prospect</li>
 * </ul>
 *
 * It triggers downstream persona processing workflows.
 * </p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/data/pipeline")
@Tag(name = "Data Pipeline APIs", description = "APIs for processing overlap records and triggering persona pipeline")
public class DataPipeLineRestController {

    private final DataPipeLineService dataPipeLineService;
    private final HubspotService hubspotService;

    /**
     * Save overlap records for Opportunity and trigger persona initialization.
     *
     * @param request overlap records request
     * @return saved overlap record response
     */
    @PostMapping("/opportunity")
    @Operation(
            summary = "Save Opportunity Overlap Records",
            description = "Stores overlap records for opportunities and initializes persona processing"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed opportunity records"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SharkdomApiResponse<OverlapRecordEntity>> saveOpportunityOverlap(
            @RequestBody OverlapRecordsRequest request) {

        log.info("API hit: Save Opportunity Overlap | orgId={}", request.getOrganizationId());

        OverlapRecordEntity response = dataPipeLineService.saveOverlapRecordsForOpportunity(request);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Opportunity overlap records processed successfully", response)
        );
    }

    /**
     * Save overlap records for Customer and trigger async persona processing.
     *
     * @param request overlap records request
     * @return saved overlap record response
     */
    @PostMapping("/customer")
    @Operation(
            summary = "Save Customer Overlap Records",
            description = "Stores overlap records for customers and triggers async persona processing"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed customer records"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SharkdomApiResponse<OverlapRecordEntity>> saveCustomerOverlap(
            @RequestBody OverlapRecordsRequest request) {

        log.info("API hit: Save Customer Overlap | orgId={}", request.getOrganizationId());

        OverlapRecordEntity response = dataPipeLineService.saveOverlapRecordsForCustomer(request);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Customer overlap records processed successfully", response)
        );
    }

    /**
     * Save overlap records for Prospect.
     *
     * @param request overlap records request
     * @return saved overlap record response
     */
    @PostMapping("/prospect")
    @Operation(
            summary = "Save Prospect Overlap Records",
            description = "Stores overlap records for prospects"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed prospect records"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SharkdomApiResponse<OverlapRecordEntity>> saveProspectOverlap(
            @RequestBody OverlapRecordsRequest request) {

        log.info("API hit: Save Prospect Overlap | orgId={}", request.getOrganizationId());

        OverlapRecordEntity response = dataPipeLineService.saveOverlapRecordsForProspect(request);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Prospect overlap records processed successfully", response)
        );
    }

    /**
     * Fetch Hubspot data dynamically based on record types.
     *
     * @param request contains list of record types
     * @return aggregated response
     */
    @PostMapping("/hubspot/column/metadata")
    @Operation(
            summary = "Fetch HubSpot Metadata Dynamically",
            description = "Fetches Companies, Contacts, Deals based on provided record types"
    )
    public ResponseEntity<SharkdomApiResponse<Map<String, Object>>> getHubspotMetadata(
            @RequestBody HubspotMetadataRequest request) {

        log.info("API hit: Fetch HubSpot metadata | recordTypes={}", request.getRecordTypes());

        Map<String, Object> data =
                hubspotService.getHubspotDataByRecordTypes(request.getRecordTypes());

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "HubSpot data fetched successfully", data)
        );
    }


    @PostMapping("/hubspot/column/data")
    @Operation(
            summary = "Fetch HubSpot Persona Data (Bulk)",
            description = "Fetches companies, contacts, deals based on multiple record types"
    )
    public ResponseEntity<SharkdomApiResponse<Map<String, Object>>> fetchPersona(
            @RequestParam RecordType recordType,
            @RequestParam String properties) {

        log.info("API hit: Persona Fetch | recordType={} | properties={}",
                recordType, properties);

        Map<String, Object> data =
                hubspotService.fetchHubspotPersonaDataBulk(recordType, properties);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Persona data fetched successfully", data)
        );
    }
}
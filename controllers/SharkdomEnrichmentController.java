package com.sharkdom.partnerattribution.controllers;

import com.sharkdom.partnerattribution.dto.*;
import com.sharkdom.partnerattribution.service.SharkdomEnrichmentService;
import com.sharkdom.util.SharkdomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Enrichment APIs.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/enrichment")
public class SharkdomEnrichmentController {

    private final SharkdomEnrichmentService enrichmentService;

    @GetMapping("/health")
    @Operation(summary = "Check enrichment service health")
    public ResponseEntity<SharkdomApiResponse<SharkdomEnrichmentHealthResponse>> health() {

        log.info("API hit: Enrichment Health");

        var data = enrichmentService.getHealth();

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Health fetched successfully", data)
        );
    }

    @GetMapping("/info")
    @Operation(summary = "Get enrichment service info")
    public ResponseEntity<SharkdomApiResponse<SharkdomEnrichmentInfoResponse>> info() {

        log.info("API hit: Enrichment Info");

        var data = enrichmentService.getServiceInfo();

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Info fetched successfully", data)
        );
    }

    @GetMapping("/departments")
    @Operation(summary = "Get available departments")
    public ResponseEntity<SharkdomApiResponse<DepartmentsResponse>> getDepartments() {

        log.info("API hit: Get Departments");

        var data = enrichmentService.getDepartments();

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Departments fetched successfully", data)
        );
    }

    @PostMapping("/enrich")
    @Operation(summary = "Enrich organization with decision makers")
    public ResponseEntity<SharkdomApiResponse<EnrichmentResponse>> enrich(
            @RequestBody EnrichmentRequest request) {

        log.info("API hit: Enrich | orgName={}", request.getOrgName());

        var data = enrichmentService.enrich(request);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true, "Enrichment successful", data)
        );
    }


}

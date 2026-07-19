package com.sharkdom.mypartner.controller;

import com.sharkdom.mypartner.dto.*;
import com.sharkdom.mypartner.entity.MyPartnerSegment;
import com.sharkdom.mypartner.service.MyPartnerSegmentService;
import com.sharkdom.mypartner.service.MyPartnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/myPartner")
public class MyPartnerRestController {

    @Autowired
    private MyPartnerService myPartnerService;

    @Autowired
    private MyPartnerSegmentService myPartnerSegmentService;

    @Operation(
            summary = "Send Partner Credentials",
            description = "Sends credentials (username, password, url) to the partner's ADMIN user via email and stores it securely."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credentials sent successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/sendCredentials")
    public ResponseEntity<?> sendPartnerCredentials(@RequestBody SendPartnerCredentialDTO sendPartnerCredentialDTO) {
        log.info("Received request to send partner credentials for partnerId: {}", sendPartnerCredentialDTO.getPartnerId());

        try {
            myPartnerService.sendCredential(sendPartnerCredentialDTO);
            log.info("Credentials sent successfully for partnerId: {}", sendPartnerCredentialDTO.getPartnerId());
            return ResponseEntity.ok("Credentials sent successfully!");
        } catch (IllegalArgumentException e) {
            log.error("Validation failed while sending credentials: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error occurred while sending credentials to partnerId {}: {}",
                    sendPartnerCredentialDTO.getPartnerId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send credentials. Please try again later.");
        }
    }

    @PostMapping("/segments/upsert")
    @Operation(summary = "Create or update multiple partner segments")
    public ResponseEntity<List<MyPartnerSegment>> createOrUpdateSegments(
            @RequestBody List<CreateMyPartnerSegmentDTO> dtoList) {
        log.info("Creating or updating {} segments", dtoList.size());
        List<MyPartnerSegment> result = myPartnerSegmentService.createOrUpdateSegments(dtoList);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/segments")
    public ResponseEntity<PaginatedResponse<MyPartnerSegment>> getByOrganizationId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "creationTimestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("Fetching segments for organization with pagination - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        return ResponseEntity.ok(
                myPartnerSegmentService.getSegmentsByOrganizationId( page, size, sortBy, sortDir)
        );
    }

    @Operation(summary = "Delete segment by ID")
    @DeleteMapping("/segments/delete/{id}")
    public ResponseEntity<String> deleteSegmentById(@PathVariable Long id) {
        log.info("Deleting segment with id={}", id);
        myPartnerSegmentService.deleteSegmentById(id);
        return ResponseEntity.ok("Segment deleted successfully.");
    }

    @PutMapping("/segments/{id}")
    @Operation(summary = "Update a single partner segment by ID")
    public ResponseEntity<MyPartnerSegment> updateSegment(
            @PathVariable Long id,
            @RequestBody CreateMyPartnerSegmentDTO dto) {
        MyPartnerSegment updatedSegment = myPartnerSegmentService.updateSegment(id, dto);
        return ResponseEntity.ok(updatedSegment);
    }

    @Operation(summary = "Send Partner training Credentials")
    @PostMapping("/send/traning/credentials")
    public ResponseEntity<?> sendPartnerCredentialsToMyPartner(@RequestBody SendPartnerTraningCredentialDTO sendPartnerCredentialDTO) {
        log.info("Received request to send partner credentials for partnerId: {}", sendPartnerCredentialDTO.getPartnerId());

        try {
            myPartnerService.sendPartnerTrainingCredential(sendPartnerCredentialDTO);
            log.info("Credentials sent successfully for partnerId: {}", sendPartnerCredentialDTO.getPartnerId());
            return ResponseEntity.ok("Credentials sent successfully!");
        } catch (IllegalArgumentException e) {
            log.error("Validation failed while sending credentials: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error occurred while sending credentials to partnerId {}: {}",
                    sendPartnerCredentialDTO.getPartnerId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send credentials. Please try again later.");
        }
    }

    @Operation(summary = "Get Partner MOU & Credential Status")
    @GetMapping("/partner/status/{partnerId}")
    public ResponseEntity<PartnerStatusResponseDTO> getPartnerStatus(
            @PathVariable String partnerId) {

        PartnerStatusResponseDTO response = myPartnerService.getPartnerStatus(partnerId);

        return ResponseEntity.ok(response);
    }

}

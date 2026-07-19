package com.sharkdom.controller;

import com.sharkdom.entity.organization.Organization;
import com.sharkdom.repository.organization.OrganizationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private final OrganizationRepository organizationRepository;

    public HealthController(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Operation(summary = "Check health of application, used by elastick beanstalk")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application is healthy.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Organization.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/")
    public ResponseEntity<Boolean> checkHealth() throws Exception {
        return ResponseEntity.ok(organizationRepository.existsOrganizationByName("string"));
        //return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
    }
}

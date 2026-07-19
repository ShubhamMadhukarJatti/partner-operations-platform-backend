package com.sharkdom.profilesection.controllers;

import com.sharkdom.profilesection.dto.*;
import com.sharkdom.profilesection.service.OrganizationCertificationService;
import com.sharkdom.profilesection.service.OrganizationProfileAggregatorService;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.SharkdomPaginatedResponse;
import com.sharkdom.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/org-certifications")
@RequiredArgsConstructor
public class OrganizationCertificationController {

    private final OrganizationCertificationService service;
    private final OrganizationProfileAggregatorService aggregatorService;

    @Operation(summary = "Get Full Organization Profile by OrgId (Admin/Internal API)")
    @GetMapping("/profile/{orgId}")
    public SharkdomApiResponse<OrganizationProfileResponse> getFullProfile(
            @PathVariable Long orgId) {

        return new SharkdomApiResponse<>(
                true,
                "Organization profile fetched successfully",
                aggregatorService.getFullProfile(orgId)
        );
    }

    @Operation(summary = "Create Certification")
    @PostMapping
    public SharkdomApiResponse<OrganizationCertificationResponse> create(
            @Valid @RequestBody OrganizationCertificationRequest request) {

        return new SharkdomApiResponse<>(true, "Certification created successfully",
                service.createCertification(request));
    }

    @Operation(summary = "Update Certification")
    @PutMapping("/{id}")
    public SharkdomApiResponse<OrganizationCertificationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationCertificationRequest request) {

        return new SharkdomApiResponse<>(true, "Certification updated successfully",
                service.updateCertification(id, request));
    }

    @Operation(summary = "Delete Certification")
    @DeleteMapping("/{id}")
    public SharkdomApiResponse<Void> delete(@PathVariable Long id) {

        service.deleteCertification(id);
        return new SharkdomApiResponse<>(true, "Certification deleted successfully", null);
    }

    @Operation(summary = "Get Certification by ID")
    @GetMapping("/{id}")
    public SharkdomApiResponse<OrganizationCertificationResponse> get(@PathVariable Long id) {

        return new SharkdomApiResponse<>(true, "Certification fetched successfully",
                service.getCertification(id));
    }

    @Operation(summary = "Get Certifications by OrgId")
    @GetMapping("/by-org/verified/{orgId}")
    public SharkdomApiResponse<?> getByOrgVerified(@PathVariable Long orgId) {
        return new SharkdomApiResponse<>(true,
                "Certifications fetched successfully",
                service.getByOrgId(orgId));
    }

    @Operation(summary = "Get Certifications by OrgId for pending status")
    @GetMapping("/by-org/pending/{orgId}")
    public SharkdomApiResponse<?> getByOrgPending(@PathVariable Long orgId) {
        return new SharkdomApiResponse<>(true,
                "Certifications fetched successfully",
                service.getByOrgIdPending(orgId));
    }

    @Operation(summary = "Get Certifications for all status")
    @GetMapping("/by-org/all")
    public SharkdomApiResponse<?> getByOrg() {
        var orgId = Util.getOrgIdFromToken();
        return new SharkdomApiResponse<>(true,
                "Certifications fetched successfully",
                service.getByOrgIdPending(orgId));
    }

    @Operation(summary = "Update Certification Verification Status (Admin)")
    @PatchMapping("/{id}/status")
    public SharkdomApiResponse<OrganizationCertificationResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody CertificationStatusUpdateRequest request) {
        return new SharkdomApiResponse<>(
                true,
                "Certification status updated successfully",
                service.updateVerificationStatus(id, request)
        );
    }

    @Operation(summary = "Get All Pending Certifications (Admin)")
    @GetMapping("/pending")
    public SharkdomApiResponse<SharkdomPaginatedResponse<OrganizationCertificationResponse>> getPendingCertifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return new SharkdomApiResponse<>(
                true,
                "Pending certifications fetched successfully",
                service.getAllPendingCertifications(page, size)
        );
    }
}
package com.sharkdom.profilesection.controllers;

import com.sharkdom.profilesection.dto.*;
import com.sharkdom.profilesection.service.OrganizationResourceService;
import com.sharkdom.util.SharkdomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/org-resources")
@RequiredArgsConstructor
public class OrganizationResourceController {

    private final OrganizationResourceService service;

    @Operation(summary = "Create Resource")
    @PostMapping
    public SharkdomApiResponse<OrganizationResourceResponse> create(
            @Valid @RequestBody OrganizationResourceRequest request) {

        return new SharkdomApiResponse<>(true,
                "Resource created successfully",
                service.create(request));
    }

    @Operation(summary = "Update Resource")
    @PutMapping("/{id}")
    public SharkdomApiResponse<OrganizationResourceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationResourceRequest request) {

        return new SharkdomApiResponse<>(true,
                "Resource updated successfully",
                service.update(id, request));
    }

    @Operation(summary = "Delete Resource")
    @DeleteMapping("/{id}")
    public SharkdomApiResponse<Void> delete(@PathVariable Long id) {

        service.delete(id);

        return new SharkdomApiResponse<>(true,
                "Resource deleted successfully",
                null);
    }

    @Operation(summary = "Get Resource by ID")
    @GetMapping("/{id}")
    public SharkdomApiResponse<OrganizationResourceResponse> get(@PathVariable Long id) {

        return new SharkdomApiResponse<>(true,
                "Resource fetched successfully",
                service.get(id));
    }


    @Operation(summary = "Get Resources by OrgId")
    @GetMapping("/by-org/{orgId}")
    public SharkdomApiResponse<?> getByOrg(@PathVariable Long orgId) {
        return new SharkdomApiResponse<>(true,
                "Resources fetched successfully",
                service.getByOrgId(orgId));
    }
}
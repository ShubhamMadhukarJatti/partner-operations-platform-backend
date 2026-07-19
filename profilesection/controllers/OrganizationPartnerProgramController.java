package com.sharkdom.profilesection.controllers;

import com.sharkdom.profilesection.dto.*;
import com.sharkdom.profilesection.service.OrganizationPartnerProgramService;
import com.sharkdom.util.SharkdomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/org-partner-program")
@RequiredArgsConstructor
public class OrganizationPartnerProgramController {

    private final OrganizationPartnerProgramService service;

    @Operation(summary = "Create or Update Partner Program")
    @PostMapping
    public SharkdomApiResponse<OrganizationPartnerProgramResponse> upsert(
            @Valid @RequestBody OrganizationPartnerProgramRequest request) {

        return new SharkdomApiResponse<>(true,
                "Partner program saved successfully",
                service.upsertProgram(request));
    }

    @Operation(summary = "Get Partner Program")
    @GetMapping
    public SharkdomApiResponse<OrganizationPartnerProgramResponse> get() {

        return new SharkdomApiResponse<>(true,
                "Partner program fetched successfully",
                service.getProgram());
    }

    @Operation(summary = "Enable/Disable Partner Program")
    @PatchMapping("/status")
    public SharkdomApiResponse<Void> toggle(@RequestParam Boolean isActive) {

        service.toggleProgramStatus(isActive);

        return new SharkdomApiResponse<>(true,
                "Partner program status updated successfully",
                null);
    }

    @Operation(summary = "Get Partner Program by OrgId")
    @GetMapping("/by-org/{orgId}")
    public SharkdomApiResponse<?> getByOrg(@PathVariable Long orgId) {
        return new SharkdomApiResponse<>(true,
                "Partner program fetched successfully",
                service.getByOrgId(orgId));
    }
}
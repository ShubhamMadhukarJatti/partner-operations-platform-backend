package com.sharkdom.profilesection.controllers;

import com.sharkdom.profilesection.dto.UserSidebarConfigDto;
import com.sharkdom.profilesection.service.UserSidebarConfigService;
import com.sharkdom.util.SharkdomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sidebar-config")
@RequiredArgsConstructor
@Slf4j
public class UserSidebarConfigController {

    private final UserSidebarConfigService service;

    /**
     * Create Sidebar Configuration
     */
    @Operation(summary = "Create sidebar configuration for a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sidebar config created successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<SharkdomApiResponse<UserSidebarConfigDto>> create(
            @RequestBody UserSidebarConfigDto request) {

        log.info("POST /api/sidebar-config | userId={}", request.getUserId());

        UserSidebarConfigDto response = service.create(request);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Sidebar config created successfully",
                        response
                )
        );
    }

    /**
     * Update Sidebar Configuration (Vendor / Partner View)
     */
    @Operation(summary = "Update sidebar configuration based on user role (Vendor or Partner)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sidebar config updated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping
    public ResponseEntity<SharkdomApiResponse<UserSidebarConfigDto>> update(
            @RequestBody UserSidebarConfigDto request) {

        log.info("PUT /api/sidebar-config | userId={} | isVendor={} | isPartner={}",
                request.getUserId(),
                request.getIsVendorView(),
                request.getIsPartnerView());

        UserSidebarConfigDto response = service.update(request);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Sidebar config updated successfully",
                        response
                )
        );
    }


    /**
     * Get Sidebar Configuration by Role (Vendor / Partner)
     */
    @Operation(summary = "Get sidebar configuration by userId and role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sidebar config fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Config not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/role/{userId}")
    public ResponseEntity<SharkdomApiResponse<UserSidebarConfigDto>> getByUserIdAndRole(
            @PathVariable String userId,
            @RequestParam(required = false) Boolean isPartner,
            @RequestParam(required = false) Boolean isVendor) {

        log.info("GET /api/sidebar-config/role/{} | isPartner={} | isVendor={}",
                userId, isPartner, isVendor);

        UserSidebarConfigDto response =
                service.getByUserIdAndRole(userId, isPartner, isVendor);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Sidebar config fetched successfully",
                        response
                )
        );
    }
}
package com.sharkdom.onboarding.controller;

import com.sharkdom.onboarding.entity.OnboardingData;
import com.sharkdom.onboarding.model.OnboardingStepRequest;
import com.sharkdom.onboarding.model.UserOrganizationRoleDto;
import com.sharkdom.onboarding.service.OnboardingService;
import com.sharkdom.onboarding.service.UserOrganizationRoleService;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.UrlNormalizer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** @author Ayush Shrivastava */

@Slf4j
@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final UserOrganizationRoleService userOrganizationRoleService;

    @Operation(summary = "Submit onboarding data",
            description = "Submits full onboarding data including name, company URL, and partnership preferences")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saved"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "500", description = "Error")
    })
    @PostMapping("/start")
    public ResponseEntity<SharkdomApiResponse<OnboardingData>> submitOnboarding(
            @Valid @RequestBody OnboardingStepRequest request) {

        log.info("POST /api/onboarding/start | companyURL={}", request.getCompanyURL());

        OnboardingData saved = onboardingService.saveOnboardingData(request);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true,
                        "Onboarding submitted successfully!",
                        saved)
        );
    }

    @GetMapping("/website/check")
    public ResponseEntity<SharkdomApiResponse<Boolean>> checkWebsiteAvailability(
            @RequestParam String website) {

        if (website == null || website.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new SharkdomApiResponse<>(false,
                            "Website parameter is required",
                            null));
        }

        String normalized = UrlNormalizer.normalize(website);

        log.info("GET /api/onboarding/website/check | raw={} | normalized={}",
                website, normalized);

        boolean available = onboardingService.isWebsiteAvailable(normalized);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true,
                        available ? "Website is available" : "Website is already registered",
                        available)
        );
    }

    @Operation(summary = "Complete onboarding",
            description = "Updates user, creates org, maps user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Completed"),
            @ApiResponse(responseCode = "400", description = "Invalid email"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Error")
    })
    @PostMapping("/complete/{email}")
    public ResponseEntity<SharkdomApiResponse<Void>> completeOnboarding(
            @PathVariable String email) {

        onboardingService.handlePostRegistration(email);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true,
                        "Onboarding completed successfully",
                        null)
        );
    }


    // =========================
    // USER ROLE APIs
    // =========================

    @Operation(summary = "Create or update user organization view.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saved"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Error")
    })
    @PostMapping("/user-view")
    public ResponseEntity<SharkdomApiResponse<UserOrganizationRoleDto>> createOrUpdateUserRole(
            @Valid @RequestBody UserOrganizationRoleDto request) {

        log.info("POST /api/onboarding/user-role | userId={} orgId={}",
                request.getUserId(), request.getOrgId());

        UserOrganizationRoleDto response =
                userOrganizationRoleService.createOrUpdate(request);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true,
                        "User role saved successfully",
                        response)
        );
    }


    @Operation(summary = "Get user view by userId")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fetched"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Error")
    })
    @GetMapping("/user-view/{userId}")
    public ResponseEntity<SharkdomApiResponse<UserOrganizationRoleDto>> getUserRole(
            @PathVariable String userId) {

        log.info("GET /api/onboarding/user-role | userId={}", userId);

        UserOrganizationRoleDto response =
                userOrganizationRoleService.getByUserId(userId);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(true,
                        "User role fetched successfully",
                        response)
        );
    }


    @Operation(summary = "Toggle user view between Vendor and Partner")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role switched successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Error")
    })
    @PutMapping("/user/toggle-role/{userId}")
    public ResponseEntity<SharkdomApiResponse<UserOrganizationRoleDto>> toggleUserView(
            @PathVariable String userId) {

        log.info("PUT /api/onboarding/user-role/toggle | userId={}", userId);

        UserOrganizationRoleDto response =
                userOrganizationRoleService.toggleVendorPartner(userId);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "User role switched successfully",
                        response
                )
        );
    }

    @Operation(summary = "Get current view of user (Vendor or Partner)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fetched"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "500", description = "Error")
    })
    @GetMapping("/user/current-role/{userId}")
    public ResponseEntity<SharkdomApiResponse<UserOrganizationRoleDto>> getCurrentUserView(
            @PathVariable String userId) {

        log.info("GET /api/onboarding/user-role/user/current-role | userId={}", userId);

        UserOrganizationRoleDto response =
                userOrganizationRoleService.getByUserViewById(userId);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "User current role fetched successfully",
                        response
                )
        );
    }

}
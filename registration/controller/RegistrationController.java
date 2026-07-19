package com.sharkdom.registration.controller;

import com.sharkdom.chatbot.dto.ChatbotRequestDto;
import com.sharkdom.chatbot.dto.ChatbotResponseDto;
import com.sharkdom.chatbot.service.SharkdomChatbotService;
import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.entity.user.OrganizationUserRoleMapping;
import com.sharkdom.registration.model.*;
import com.sharkdom.registration.service.RegistrationService;
import com.sharkdom.security.LoginAuditService;
import com.sharkdom.service.ppi.DnsUtil;
import com.sharkdom.util.SharkdomApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/users")
@Validated
@Slf4j
public class RegistrationController {
    private final RegistrationService registrationService;
    private final SharkdomChatbotService sharkdomChatbotService;
    private final DnsUtil dnsUtil;
    private final LoginAuditService loginAuditService;

    public RegistrationController(RegistrationService registrationService, SharkdomChatbotService sharkdomChatbotService, DnsUtil dnsUtil, LoginAuditService loginAuditService) {
        this.registrationService = registrationService;
        this.sharkdomChatbotService = sharkdomChatbotService;
        this.dnsUtil = dnsUtil;
        this.loginAuditService = loginAuditService;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody @Valid UserLoginRequest request) {
        return registrationService.loginUser(request);
    }

    @PostMapping("/direct/login")
    public LoginResponse directLogin(@RequestParam String email) {
        return registrationService.directLogin(email);
    }

    @PostMapping("/partner-portal/login")
    public Map<String, String> partnerPortalLogin(@RequestBody @Valid UserLoginRequest request) {
        return registrationService.partnerPortalLogin(request);
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody @Valid UserLoginRequest request) {
        return registrationService.registerUser(request.email());
    }

    @PostMapping("/verify")
    public LoginResponse verify(@RequestBody @Valid UserVerifyRequest request) {
        return registrationService.verifyUser(request);
    }

    @PostMapping("/external/partner/verify")
    public LoginResponse verifyExternalPartnerPortal(@RequestBody @Valid UserVerifyRequest request) {
        return registrationService.externalPartnerVerifyUser(request);
    }

    @PostMapping("/refresh-token")
    public LoginResponse generateRefreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        return registrationService.createRefreshToken(request);
    }

    @PostMapping("/organization/roles")
    public ResponseEntity<List<OrganizationUserRoleMapping>> createOrUpdateMappings(
            @RequestParam Long orgUserMappingId,
            @RequestBody List<OrgUserRole> roles) {
        List<OrganizationUserRoleMapping> saved = registrationService.createOrUpdateRoleMappings(orgUserMappingId, roles);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<List<OrganizationUserRoleMapping>> updateUserRoles(
            @PathVariable String userId,
            @RequestBody List<OrgUserRole> roles) {
        log.info("Received PUT request to update roles for userId: {} with roles: {}", userId, roles);
        List<OrganizationUserRoleMapping> updatedRoles =
                registrationService.createOrUpdateRoleMappingsByUserId(userId, roles);
        log.info("Updated {} role mappings for userId: {}", updatedRoles.size(), userId);
        return ResponseEntity.ok(updatedRoles);
    }

    @PostMapping("/chatbot/ask")
    public SharkdomApiResponse<ChatbotResponseDto> askChatbot(
            @RequestBody ChatbotRequestDto requestDto) {
        return sharkdomChatbotService.chat(requestDto);
    }

    @GetMapping("/dns/form-url")
    public ResponseEntity<SharkdomApiResponse<String>> getFormUrlByCustomDomain(
            @RequestParam String customDomain
    ) {
        String formUrl = dnsUtil.getFormUrlByCustomDomain(customDomain);
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Form URL fetched successfully",
                        formUrl
                )
        );
    }

    @GetMapping("/dns/form-id")
    public ResponseEntity<SharkdomApiResponse<String>> getFormIdByCustomDomain(
            @RequestParam String customDomain
    ) {
        String formId = dnsUtil.getFormIdByCustomDomain(customDomain);
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Form ID fetched successfully",
                        formId
                )
        );
    }

    @PostMapping("/otp/success")
    public ResponseEntity<?> otpLoginSuccess(
            @RequestParam String email,
            HttpServletRequest request
    ) throws Exception {
        loginAuditService.handleSuccessfulLogin(email, request);
        return ResponseEntity.ok("OTP login handled successfully");
    }

    @GetMapping("/admin/refreshToken/data")
    public ResponseEntity<SharkdomApiResponse<List<RefreshTokenResponse>>> getAllRefreshTokens() {

        log.info("Fetching all refresh tokens");

        List<RefreshTokenResponse> tokens = registrationService.getAllRefreshTokens();

        SharkdomApiResponse<List<RefreshTokenResponse>> response =
                new SharkdomApiResponse<>(
                        true,
                        "Refresh tokens fetched successfully",
                        tokens
                );

        return ResponseEntity.ok(response);
    }
}

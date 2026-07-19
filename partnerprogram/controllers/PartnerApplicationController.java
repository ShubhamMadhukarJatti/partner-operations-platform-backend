package com.sharkdom.partnerprogram.controllers;

import com.sharkdom.entity.organization.Organization;
import com.sharkdom.partnerprogram.dtos.*;
import com.sharkdom.partnerprogram.service.PartnerApplicationService;
import com.sharkdom.partnerprogram.service.PartnerLeadService;
import com.sharkdom.partnerprogram.service.impl.PartnerResourceService;
import com.sharkdom.partnertraining.dto.CoverImageUploadResponseDto;
import com.sharkdom.partnertraining.service.CourseService;
import com.sharkdom.registration.model.LoginResponse;
import com.sharkdom.registration.model.UserLoginRequest;
import com.sharkdom.registration.model.UserVerifyRequest;
import com.sharkdom.registration.service.RegistrationService;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.SharkdomPaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/partner")
@RequiredArgsConstructor
@Slf4j
public class PartnerApplicationController {

        private final PartnerApplicationService service;
        private final RegistrationService registrationService;
        private final PartnerLeadService partnerLeadService;
        private final PartnerResourceService partnerResourceService;
        private final CourseService courseService;
        private final UserRepository userRepository;

        @Operation(summary = "Create Partner Application")
        @PostMapping("/application")
        public SharkdomApiResponse<PartnerApplicationDTO> createPartnerApplication(
                        @RequestBody PartnerApplicationDTO dto) {
                log.info("Received request to create/update Partner Application for email: {}", dto.getEmail());

                return new SharkdomApiResponse<>(
                                true,
                                "Partner Application submitted successfully",
                                service.create(dto));
        }

        @Operation(summary = "Update Partner Application")
        @PutMapping("/application/{id}")
        public SharkdomApiResponse<PartnerApplicationDTO> updatePartnerApplication(
                        @PathVariable Long id,
                        @RequestBody PartnerApplicationDTO dto) {
                return new SharkdomApiResponse<>(
                                true,
                                "Partner Application updated successfully",
                                service.update(id, dto));
        }

        @Operation(summary = "Delete Partner Application")
        @DeleteMapping("/application/{id}")
        public SharkdomApiResponse<Void> deletePartnerApplication(
                        @PathVariable Long id) {
                service.delete(id);

                return new SharkdomApiResponse<>(
                                true,
                                "Partner Application deleted successfully",
                                null);
        }

        @Operation(summary = "Get Partner Application By Id")
        @GetMapping("/application/{id}")
        public SharkdomApiResponse<PartnerApplicationDTO> getPartnerApplicationById(
                        @PathVariable Long id) {
                return new SharkdomApiResponse<>(
                                true,
                                "Partner Application fetched successfully",
                                service.getById(id));
        }

        @Operation(summary = "Get All Partner Applications")
        @GetMapping("/applications")
        public SharkdomApiResponse<SharkdomPaginatedResponse<PartnerApplicationDTO>> getAllPartnerApplication(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                return new SharkdomApiResponse<>(
                                true,
                                "Partner Applications fetched successfully",
                                service.getAll(page, size));
        }

        @Operation(summary = "Send OTP for Partner Login")
        @PostMapping("/auth/send-otp")
        public SharkdomApiResponse<Map<String, String>> sendPartnerOtp(
                        @RequestBody @Valid UserLoginRequest request) {
                log.info("Partner OTP requested for email: {}", request.email());

                return new SharkdomApiResponse<>(
                                true,
                                "OTP sent successfully",
                                registrationService.loginUser(request));
        }

        @Operation(summary = "Verify OTP and Login/Create Partner User")
        @PostMapping("/auth/verify-otp")
        public SharkdomApiResponse<LoginResponse> verifyPartnerOtp(
                        @RequestBody @Valid UserVerifyRequest request) {
                log.info("Partner OTP verification requested for email: {}", request.email());

                return new SharkdomApiResponse<>(
                                true,
                                "Partner login successful",
                                registrationService.verifyPartner(request));
        }

        @Operation(summary = "Login Partner using Email and Password")
        @PostMapping("/auth/login")
        public SharkdomApiResponse<LoginResponse> loginPartner(
                        @RequestBody @Valid com.sharkdom.partnerprogram.dtos.PartnerPasswordLoginRequest request) {
                log.info("Partner password login requested for email: {}", request.getEmail());

                return new SharkdomApiResponse<>(
                                true,
                                "Partner login successful",
                                registrationService.partnerPasswordLogin(request));
        }

        @Operation(
            summary = "Set Password after verifying OTP",
            security = { @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "SharkdomAuth") }
        )
        @PutMapping("/auth/password")
        public SharkdomApiResponse<String> setPassword(
                        @RequestBody @Valid SetPasswordRequest request) {
                String email = com.sharkdom.util.Util.getUserFromToken();
                service.setPassword(email, request);

                return new SharkdomApiResponse<>(
                                true,
                                "Password set successfully",
                                null);
        }

        @Operation(summary = "Create Partner Lead")
        @PostMapping("/lead")
        public SharkdomApiResponse<PartnerLeadDTO> createLead(
                        @RequestBody PartnerLeadDTO dto) {
                
                String email = com.sharkdom.util.Util.getUserFromToken();
                String userId = userRepository.findByEmailAndIsActiveTrue(email)
                                .map(com.sharkdom.entity.user.User::getUserId)
                                .orElseThrow(() -> new ServiceException(ErrorMessages.SH03, email));

                dto.setUserId(userId);

                try {
                        PartnerApplicationDTO partnerApp = service.getByEmail(email);
                        if (partnerApp != null && partnerApp.getPartnershipTier() != null) {
                                dto.setPartnershipTier(partnerApp.getPartnershipTier());
                                dto.setInvolvementLevel(partnerApp.getPartnershipTier());
                        }
                } catch (Exception e) {
                        log.warn("Could not fetch partner application for email: {}", email);
                }

                dto.setSubmittedDate(new java.util.Date());

                return new SharkdomApiResponse<>(
                                true,
                                "Lead created successfully",
                                partnerLeadService.create(dto));
        }

        @Operation(summary = "Update Partner Lead")
        @PutMapping("/lead/{id}")
        public SharkdomApiResponse<PartnerLeadDTO> updateLead(
                        @PathVariable Long id,
                        @RequestBody PartnerLeadDTO dto) {
                return new SharkdomApiResponse<>(
                                true,
                                "Lead updated successfully",
                                partnerLeadService.update(id, dto));
        }

        @Operation(summary = "Delete Partner Lead")
        @DeleteMapping("/lead/{id}")
        public SharkdomApiResponse<Void> deleteLead(
                        @PathVariable Long id) {
                service.delete(id);

                return new SharkdomApiResponse<>(
                                true,
                                "Lead deleted successfully",
                                null);
        }

        @Operation(summary = "Get Lead By Id")
        @GetMapping("/lead/{id}")
        public SharkdomApiResponse<PartnerLeadDTO> getLeadById(
                        @PathVariable Long id) {
                return new SharkdomApiResponse<>(
                                true,
                                "Lead fetched successfully",
                                partnerLeadService.getById(id));
        }

        @Operation(summary = "Get All Leads By UserId")
        @GetMapping("/leads")
        public SharkdomApiResponse<SharkdomPaginatedResponse<PartnerLeadDTO>> getAllLeads(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                
                String email = com.sharkdom.util.Util.getUserFromToken();
                String userId = userRepository.findByEmailAndIsActiveTrue(email)
                                .map(com.sharkdom.entity.user.User::getUserId)
                                .orElseThrow(() -> new ServiceException(ErrorMessages.SH03, email));

                return new SharkdomApiResponse<>(
                                true,
                                "Leads fetched successfully",
                                partnerLeadService.getAll(userId, page, size));
        }

        @Operation(
                summary = "Get Recent Leads (Dashboard)",
                description = "Returns the most recently submitted leads for a partner, "
                        + "sorted by submittedDate DESC (newest first). "
                        + "Each item includes all UI display fields: statusLabel, tierDisplay, "
                        + "estimatedCommissionDisplay, actionLabel, and canXxx permission flags "
                        + "so the frontend can render the Recent Leads dashboard table directly."
        )
        @GetMapping("/recent-leads/{userId}")
        public SharkdomApiResponse<SharkdomPaginatedResponse<PartnerLeadDTO>> getRecentLeads(
                        @Parameter(description = "Partner User ID", example = "USR123456")
                        @PathVariable String userId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                return new SharkdomApiResponse<>(
                                true,
                                "Recent leads fetched successfully",
                                partnerLeadService.getRecentLeads(userId, page, size));
        }

        @PostMapping("/approve")
        @Operation(summary = "Approve Partner Account")
        public SharkdomApiResponse<Map<String, String>> approvePartner(
                        @RequestBody @Valid ApprovePartnerRequest request) {
                com.sharkdom.entity.user.User user = service.approvePartner(request.getEmail());

                return new SharkdomApiResponse<>(
                                true,
                                "Partner approved successfully",
                                Map.of("userId", user.getUserId(), "message", "Account created and approval email sent"));
        }

        @PostMapping("/disable")
        @Operation(summary = "Disable Partner Account")
        public SharkdomApiResponse<String> disablePartner(
                        @RequestBody @Valid ApprovePartnerRequest request) {
                service.disablePartner(request.getEmail());

                return new SharkdomApiResponse<>(
                                true,
                                "Partner disabled successfully",
                                "User account deactivated");
        }

        @PostMapping("/enable")
        @Operation(summary = "Enable Partner Account")
        public SharkdomApiResponse<String> enablePartner(
                        @RequestBody @Valid ApprovePartnerRequest request) {
                service.enablePartner(request.getEmail());

                return new SharkdomApiResponse<>(
                                true,
                                "Partner enabled successfully",
                                "User account activated");
        }

        @GetMapping("/dashboard/stats/{userId}")
        @Operation(summary = "Get Partner Dashboard Stats")
        public SharkdomApiResponse<PartnerDashboardStatsDTO> getDashboardStats(
                        @PathVariable String userId) {
                return new SharkdomApiResponse<>(
                                true,
                                "Dashboard stats fetched successfully",
                                service.getDashboardStats(userId));
        }

        @Operation(summary = "Create Partner Resource")
        @PostMapping("/resources")
        public SharkdomApiResponse<PartnerResourceDTO> create(
                        @RequestBody PartnerResourceDTO request) {
                log.info("Creating partner resource: {}", request.getTitle());

                return new SharkdomApiResponse<>(
                                true,
                                "Partner resource created successfully",
                                partnerResourceService.create(request));
        }

        @Operation(summary = "Update Partner Resource")
        @PutMapping("/resources/{id}")
        public SharkdomApiResponse<PartnerResourceDTO> update(
                        @PathVariable Long id,
                        @RequestBody PartnerResourceDTO request) {
                log.info("Updating partner resource id: {}", id);

                return new SharkdomApiResponse<>(
                                true,
                                "Partner resource updated successfully",
                                partnerResourceService.update(id, request));
        }

        @Operation(summary = "Delete Partner Resource")
        @DeleteMapping("/resources/{id}")
        public SharkdomApiResponse<Void> delete(
                        @PathVariable Long id) {
                log.info("Deleting partner resource id: {}", id);

                partnerResourceService.delete(id);

                return new SharkdomApiResponse<>(
                                true,
                                "Partner resource deleted successfully",
                                null);
        }

        @Operation(summary = "Get Partner Resource By Id")
        @GetMapping("/resources/{id}")
        public SharkdomApiResponse<PartnerResourceDTO> getById(
                        @PathVariable Long id) {
                log.info("Fetching partner resource id: {}", id);

                return new SharkdomApiResponse<>(
                                true,
                                "Partner resource fetched successfully",
                                partnerResourceService.getById(id));
        }

        @Operation(summary = "Get All Partner Resources")
        @GetMapping("/resources")
        public SharkdomApiResponse<List<PartnerResourceDTO>> getAll() {
                log.info("Fetching all partner resources");

                return new SharkdomApiResponse<>(
                                true,
                                "Partner resources fetched successfully",
                                partnerResourceService.getAll());
        }

        @Operation(summary = "Upload File", description = "Uploads a file to S3 and returns the public file URL")
        @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public SharkdomApiResponse<CoverImageUploadResponseDto> uploadFile(
                        @RequestParam("file") MultipartFile file) {
                log.info("Uploading file: name={}, size={}",
                                file.getOriginalFilename(), file.getSize());
                CoverImageUploadResponseDto response = courseService.uploadFile(file);
                log.info("File uploaded successfully. URL={}", response.getFileUrl());
                return new SharkdomApiResponse<>(
                                true,
                                "File uploaded successfully",
                                response);
        }

        @Operation(summary = "Get Partner Commission Stats", description = "Fetch partner commission dashboard stats including total earned, pending commission, and next payout date")
        @GetMapping("/commission/stats/{userId}")
        public SharkdomApiResponse<PartnerCommissionStatsDTO> getStats(
                        @Parameter(description = "Partner User ID", example = "USR123456") @PathVariable String userId) {
                return new SharkdomApiResponse<>(
                                true,
                                "Commission stats fetched successfully",
                                partnerLeadService.getCommissionStats(userId));
        }

        @Operation(summary = "Get Partner Commission List", description = "Fetch paginated commission records for a specific partner")
        @GetMapping("/commissions/{userId}")
        public SharkdomApiResponse<SharkdomPaginatedResponse<PartnerCommissionDTO>> getCommissions(
                        @Parameter(description = "Partner User ID", example = "USR123456") @PathVariable String userId,

                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                return new SharkdomApiResponse<>(
                                true,
                                "Commission list fetched successfully",
                                partnerLeadService.getCommissions(userId, page, size));
        }

        @Operation(
            summary = "Get Current Logged-in Partner Profile",
            security = { @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "SharkdomAuth") }
        )
        @GetMapping("/me")
        public SharkdomApiResponse<PartnerApplicationDTO> getCurrentUser() {
                String email = com.sharkdom.util.Util.getUserFromToken();
                log.info("Fetching partner profile for currently logged in user: {}", email);

                return new SharkdomApiResponse<>(
                                true,
                                "Profile fetched successfully",
                                service.getByEmail(email));
        }

        @Operation(summary = "Get Partner Application By UserId")
        @GetMapping("/user/{userId}")
        public SharkdomApiResponse<PartnerApplicationDTO> getByUserId(
                        @PathVariable String userId) {
                log.info("Fetching partner application for userId: {}", userId);

                return new SharkdomApiResponse<>(
                                true,
                                "Partner application fetched successfully",
                                service.getByUserId(userId));
        }

        @Operation(summary = "Update Partner Application By UserId")
        @PutMapping("/user/{userId}")
        public SharkdomApiResponse<PartnerApplicationDTO> updateByUserId(
                        @PathVariable String userId,
                        @RequestBody PartnerApplicationDTO dto) {
                log.info("Updating partner application for userId: {}", userId);

                return new SharkdomApiResponse<>(
                                true,
                                "Partner application updated successfully",
                                service.updateByUserId(userId, dto));
        }

        @Operation(summary = "Update referral code by email")
        @PutMapping("/referral-code")
        public SharkdomApiResponse<Organization> updateReferralCode(
                        @RequestBody UpdateReferralCodeRequest request) {
                return new SharkdomApiResponse<>(
                                true,
                                "Referral code updated successfully",
                                service.updateReferralCodeByEmail(request));
        }

        @Operation(summary = "Ask for payment email")
        @PostMapping("/ask-for-payment")
        public SharkdomApiResponse<Void> askForPayment(
                        @RequestBody AskForPaymentRequestDTO request) {
                service.askForPayment(request);

                return new SharkdomApiResponse<>(
                                true,
                                "Payment request email sent successfully",
                                null);
        }
}
package com.sharkdom.partnerprogram.service.impl;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.user.UserRole;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.user.User;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.partnerprogram.dtos.AskForPaymentRequestDTO;
import com.sharkdom.partnerprogram.dtos.PartnerApplicationDTO;
import com.sharkdom.partnerprogram.dtos.PartnerDashboardStatsDTO;
import com.sharkdom.partnerprogram.dtos.UpdateReferralCodeRequest;
import com.sharkdom.partnerprogram.dtos.SetPasswordRequest;
import com.sharkdom.partnerprogram.entities.PartnerApplication;
import com.sharkdom.partnerprogram.entities.ReferralCodeUtil;
import com.sharkdom.partnerprogram.enums.LeadStatus;
import com.sharkdom.partnerprogram.repository.PartnerApplicationRepository;
import com.sharkdom.partnerprogram.repository.PartnerLeadRepository;
import com.sharkdom.partnerprogram.service.PartnerApplicationService;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.reseller.entity.PaymentStatus;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.util.SharkdomPaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerApplicationServiceImpl implements PartnerApplicationService {

        private final PartnerApplicationRepository repository;
        private final EmailService emailService;
        private final UserRepository userRepository;
        private final PartnerLeadRepository partnerLeadRepository;
        private final ReferralCodeUtil referralCodeUtil;
        private final OrganizationRepository organizationRepository;

        @Override
        public PartnerApplicationDTO create(PartnerApplicationDTO dto) {
                log.info("Create/Update PartnerApplication for email: {}", dto.getEmail());

                PartnerApplication entity = repository.findByEmail(dto.getEmail())
                                .map(existing -> {
                                        log.info("PartnerApplication already exists for email {}, updating existing record",
                                                        dto.getEmail());
                                        updateEntity(existing, dto);
                                        return existing;
                                })
                                .orElseGet(() -> {
                                        log.info("Creating new PartnerApplication for email {}", dto.getEmail());
                                        return mapToEntity(dto);
                                });

                PartnerApplication savedEntity = repository.save(entity);

                try {
                        // 1. Send email to internal team (Deepak / CSM)
                        emailService.sendPartnerCreatedEmail(
                                        "COMPANY_PARTNER_CREATED_TO_CSM",
                                        savedEntity,
                                        "deepak.v@sharkdom.com");

                        // 2. Send confirmation email to user
                        emailService.sendTemplateWithUserName(
                                        "COMPANY_PARTNER_CREATED",
                                        savedEntity.getEmail(),
                                        savedEntity.getCompanyName());

                } catch (Exception e) {
                        log.error("Error while sending email for CompanyPartnerApplication id: {}, error: {}",
                                        savedEntity.getId(), e.getMessage(), e);
                }

                return mapToDTO(savedEntity);
        }

        @Override
        public PartnerApplicationDTO update(Long id, PartnerApplicationDTO dto) {
                log.info("Updating PartnerApplication id: {}", id);

                PartnerApplication entity = repository.findById(id)
                                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

                updateEntity(entity, dto);

                PartnerApplication updatedEntity = repository.save(entity);

                return mapToDTO(updatedEntity);
        }

        @Override
        public void delete(Long id) {
                log.info("Deleting PartnerApplication id: {}", id);

                PartnerApplication entity = repository.findById(id)
                                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

                repository.delete(entity);
        }

        @Override
        public PartnerApplicationDTO getById(Long id) {
                log.info("Fetching PartnerApplication id: {}", id);

                PartnerApplication entity = repository.findById(id)
                                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

                return mapToDTO(entity);
        }

        @Override
        public SharkdomPaginatedResponse<PartnerApplicationDTO> getAll(int page, int size) {
                log.info("Fetching PartnerApplications page: {}, size: {}", page, size);

                Page<PartnerApplication> pageData = repository.findAll(
                                PageRequest.of(
                                                page,
                                                size,
                                                Sort.by("id").descending()));

                SharkdomPaginatedResponse<PartnerApplicationDTO> response = new SharkdomPaginatedResponse<>();

                response.setContent(
                                pageData.getContent()
                                                .stream()
                                                .map(this::mapToDTO)
                                                .collect(Collectors.toList()));

                response.setPage(page);
                response.setSize(size);
                response.setTotalElements(pageData.getTotalElements());
                response.setTotalPages(pageData.getTotalPages());
                response.setLast(pageData.isLast());

                return response;
        }

        private PartnerApplication mapToEntity(PartnerApplicationDTO dto) {
                return PartnerApplication.builder()
                                .fullName(dto.getFullName())
                                .email(dto.getEmail())
                                .linkedInProfileUrl(dto.getLinkedInProfileUrl())
                                .companyName(dto.getCompanyName())
                                .geography(dto.getGeography())
                                .companiesAdvised(dto.getCompaniesAdvised())
                                .primaryGtmFocus(dto.getPrimaryGtmFocus())
                                .howDidYouHearAboutProgram(dto.getHowDidYouHearAboutProgram())
                                .partnershipTier(dto.getPartnershipTier())
                                .networkDescription(dto.getNetworkDescription())
                                .build();
        }

        private void updateEntity(
                        PartnerApplication entity,
                        PartnerApplicationDTO dto) {
                entity.setFullName(dto.getFullName());
                entity.setEmail(dto.getEmail());
                entity.setLinkedInProfileUrl(dto.getLinkedInProfileUrl());
                entity.setCompanyName(dto.getCompanyName());
                entity.setGeography(dto.getGeography());
                entity.setCompaniesAdvised(dto.getCompaniesAdvised());
                entity.setPrimaryGtmFocus(dto.getPrimaryGtmFocus());
                entity.setHowDidYouHearAboutProgram(dto.getHowDidYouHearAboutProgram());
                entity.setPartnershipTier(dto.getPartnershipTier());
                entity.setNetworkDescription(dto.getNetworkDescription());
        }

        private PartnerApplicationDTO mapToDTO(
                        PartnerApplication entity) {
                PartnerApplicationDTO dto = new PartnerApplicationDTO();

                dto.setId(entity.getId());
                dto.setFullName(entity.getFullName());
                dto.setEmail(entity.getEmail());
                dto.setLinkedInProfileUrl(entity.getLinkedInProfileUrl());
                dto.setCompanyName(entity.getCompanyName());
                dto.setGeography(entity.getGeography());
                dto.setCompaniesAdvised(entity.getCompaniesAdvised());
                dto.setPrimaryGtmFocus(entity.getPrimaryGtmFocus());
                dto.setHowDidYouHearAboutProgram(entity.getHowDidYouHearAboutProgram());
                dto.setUserId(entity.getUserId());
                dto.setPartnershipTier(entity.getPartnershipTier());
                dto.setNetworkDescription(entity.getNetworkDescription());
                if (entity.getUserId() != null) {
                        dto.setActive(userRepository.existsByUserIdAndIsActiveTrue(entity.getUserId()));
                        var referralCode = referralCodeUtil.generateReferralCode(entity.getUserId());
                        dto.setReferCode(referralCode);
                } else {
                        dto.setActive(false);
                        dto.setReferCode(null);
                }
                return dto;
        }

        @Transactional
        public User approvePartner(String email) {

                User user = userRepository.findByEmail(email)
                                .map(existingUser -> {
                                        existingUser.setActive(true);
                                        existingUser.setEmailVerified(true);
                                        existingUser.setUserType(UserRole.PARTNER.name());

                                        return userRepository.save(existingUser);
                                })
                                .orElseGet(() -> createPartnerUser(email));

                if (user != null) {
                        log.info("Partner user with email {} approved successfully", email);
                        var optPartnerApplication = repository.findByEmail(email);
                        if (optPartnerApplication.isPresent()) {
                                var partnerApplication = optPartnerApplication.get();
                                partnerApplication.setUserId(user.getUserId());
                                repository.save(partnerApplication);
                                log.info("Linked PartnerApplication id {} with userId {}", partnerApplication.getId(),
                                                user.getUserId());
                        }

                }

                try {
                        // emailService.sendPartnerApprovalEmail(
                        // "PARTNER_ACCOUNT_APPROVED",
                        // user.getEmail(),
                        // Map.of(
                        // "dashboardLink", "https://partner.sharkdom.com/login",
                        // "email", user.getEmail()
                        // )
                        // );

                        log.info("Partner approval email sent successfully to {}", email);

                } catch (Exception e) {
                        log.error("Failed to send partner approval email for email {}", email, e);
                }

                return user;
        }

        public String generateUserId() {
                return UUID.randomUUID().toString();
        }

        private User createPartnerUser(String email) {
                User newUser = new User();

                newUser.setUserId(generateUserId());
                newUser.setEmail(email);
                newUser.setEmailVerified(true);
                newUser.setActive(true);
                newUser.setUserType(UserRole.PARTNER.name());

                return userRepository.save(newUser);
        }

        @Transactional
        public void disablePartner(String email) {
                log.info("Disabling partner user with email {}", email);
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ServiceException(
                                                ErrorMessages.SH03,
                                                email));

                user.setActive(false);

                userRepository.save(user);
                log.info("Partner user with email {} disabled successfully", email);
        }

        @Transactional
        public void enablePartner(String email) {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ServiceException(
                                                ErrorMessages.SH03,
                                                email));

                user.setActive(true);

                // optional: ensure partner role remains correct
                user.setUserType(UserRole.PARTNER.name());

                // optional: if email verification should be restored
                user.setEmailVerified(true);

                userRepository.save(user);

        }

        @Override
        public PartnerDashboardStatsDTO getDashboardStats(String userId) {

                // Total leads submitted by partner
                long totalLeadsSubmitted = partnerLeadRepository.countByUserId(userId);

                // Leads currently in progress
                long leadsInProgress = partnerLeadRepository
                                .countByUserIdAndLeadStatusIn(
                                                userId,
                                                List.of(
                                                                LeadStatus.ACCEPTED,
                                                                LeadStatus.DEMO_SCHEDULED,
                                                                LeadStatus.UNDER_REVIEW,
                                                                LeadStatus.ONBOARDED));

                // Total commission earned
                BigDecimal commissionEarned = partnerLeadRepository
                                .sumCommissionByUserIdAndPaymentStatus(
                                                userId,
                                                PaymentStatus.PAID);

                // Total pending commission
                BigDecimal commissionPending = partnerLeadRepository
                                .sumCommissionByUserIdAndPaymentStatus(
                                                userId,
                                                PaymentStatus.PENDING);

                return new PartnerDashboardStatsDTO(
                                (int) totalLeadsSubmitted,
                                (int) leadsInProgress,
                                commissionEarned != null ? commissionEarned : BigDecimal.ZERO,
                                commissionPending != null ? commissionPending : BigDecimal.ZERO);
        }

        @Override
        public PartnerApplicationDTO getByUserId(String userId) {

                PartnerApplication entity = repository.findByUserId(userId)
                                .orElseThrow(() -> new ServiceException(ErrorMessages.SH106));

                return mapToDTO(entity);
        }

        @Override
        public PartnerApplicationDTO getByEmail(String email) {

                PartnerApplication entity = repository.findByEmail(email)
                                .orElseThrow(() -> new ServiceException(ErrorMessages.SH106));

                return mapToDTO(entity);
        }

        @Override
        @Transactional
        public PartnerApplicationDTO updateByUserId(String userId, PartnerApplicationDTO dto) {

                PartnerApplication entity = repository.findByUserId(userId)
                                .orElseThrow(() -> new ServiceException(ErrorMessages.SH106));

                updateEntity(entity, dto);

                PartnerApplication updated = repository.save(entity);

                return mapToDTO(updated);
        }

        @Transactional
        @Override
        public Organization updateReferralCodeByEmail(UpdateReferralCodeRequest request) {

                Organization organization = organizationRepository
                                .findByPrimaryEmail(request.getEmail())
                                .orElseThrow(() -> new ServiceException(
                                                ErrorMessages.SH30,
                                                request.getEmail()));

                organization.setReferralCode(request.getReferralCode());

                return organizationRepository.save(organization);
        }

        @Transactional
        public void askForPayment(AskForPaymentRequestDTO request) {

                PartnerApplication partnerApplication = repository
                                .findByUserId(request.getUserId())
                                .orElseThrow(() -> new ServiceException(ErrorMessages.SH106));

                PartnerApplicationDTO partnerDTO = mapToDTO(partnerApplication);

                try {
                        emailService.sendAskForPaymentEmailToCompany(
                                        "PARTNER_PAYMENT_REQUEST",
                                        partnerDTO,
                                        partnerDTO.getEmail(),
                                        request.getReason(),
                                        request.getNotes());
                } catch (Exception e) {
                        throw new ServiceException(
                                        ErrorMessages.SH134,
                                        e.getMessage());
                }
        }

        @Transactional
        @Override
        public void setPassword(String email, SetPasswordRequest request) {
                if (!request.getNewPassword().equals(request.getRetypePassword())) {
                        throw new IllegalArgumentException("Passwords do not match");
                }

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ServiceException(ErrorMessages.SH03, email));

                org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

                user.setPassword(encoder.encode(request.getNewPassword()));

                userRepository.save(user);
        }

}
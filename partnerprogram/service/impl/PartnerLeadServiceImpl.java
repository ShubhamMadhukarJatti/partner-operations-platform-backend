package com.sharkdom.partnerprogram.service.impl;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.partnerprogram.dtos.PartnerCommissionDTO;
import com.sharkdom.partnerprogram.dtos.PartnerCommissionStatsDTO;
import com.sharkdom.partnerprogram.dtos.PartnerLeadDTO;
import com.sharkdom.partnerprogram.entities.PartnerLead;
import com.sharkdom.partnerprogram.enums.LeadStatus;
import com.sharkdom.partnerprogram.repository.PartnerLeadRepository;
import com.sharkdom.partnerprogram.service.PartnerLeadService;
import com.sharkdom.util.SharkdomPaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerLeadServiceImpl implements PartnerLeadService {

    private final PartnerLeadRepository repository;

    @Override
    public PartnerLeadDTO create(PartnerLeadDTO dto) {
        PartnerLead entity = mapToEntity(dto);

        if (entity.getLeadStatus() == null) {
            entity.setLeadStatus(LeadStatus.SUBMITTED);
        }

        return mapToDTO(repository.save(entity));
    }

    @Override
    public PartnerLeadDTO update(Long id, PartnerLeadDTO dto) {
        PartnerLead entity = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        updateEntity(entity, dto);

        return mapToDTO(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        PartnerLead entity = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        repository.delete(entity);
    }

    @Override
    public PartnerLeadDTO getById(Long id) {
        PartnerLead entity = repository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        return mapToDTO(entity);
    }

    @Override
    public SharkdomPaginatedResponse<PartnerLeadDTO> getAll(
            String userId,
            int page,
            int size
    ) {
        Page<PartnerLead> pageData = repository.findByUserId(
                userId,
                PageRequest.of(page, size, Sort.by("id").descending())
        );

        SharkdomPaginatedResponse<PartnerLeadDTO> response =
                new SharkdomPaginatedResponse<>();

        response.setContent(
                pageData.getContent()
                        .stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList())
        );

        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(pageData.getTotalElements());
        response.setTotalPages(pageData.getTotalPages());
        response.setLast(pageData.isLast());

        return response;
    }

    @Override
    public SharkdomPaginatedResponse<PartnerLeadDTO> getRecentLeads(
            String userId,
            int page,
            int size
    ) {
        Page<PartnerLead> pageData = repository.findRecentLeadsByUserId(
                userId,
                PageRequest.of(page, size)
        );

        SharkdomPaginatedResponse<PartnerLeadDTO> response = new SharkdomPaginatedResponse<>();

        response.setContent(
                pageData.getContent()
                        .stream()
                        .map(this::mapToRecentLeadDTO)
                        .collect(Collectors.toList())
        );

        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(pageData.getTotalElements());
        response.setTotalPages(pageData.getTotalPages());
        response.setLast(pageData.isLast());

        return response;
    }

    private PartnerLead mapToEntity(PartnerLeadDTO dto) {
        return PartnerLead.builder()
                .userId(dto.getUserId())
                .companyName(dto.getCompanyName())
                .companyWebsite(dto.getCompanyWebsite())
                .industry(dto.getIndustry())
                .companySize(dto.getCompanySize())
                .geography(dto.getGeography())
                .estimatedAcv(dto.getEstimatedAcv())
                .contactName(dto.getContactName())
                .contactTitle(dto.getContactTitle())
                .contactEmail(dto.getContactEmail())
                .contactLinkedIn(dto.getContactLinkedIn())
                .buyingIntentSignal(dto.getBuyingIntentSignal())
                .leadTemperature(dto.getLeadTemperature())
                .involvementLevel(dto.getInvolvementLevel())
                .preferredMeetingFormat(dto.getPreferredMeetingFormat())
                .messageToSharkdomTeam(dto.getMessageToSharkdomTeam())
                .estimatedCommission(dto.getEstimatedCommission())
                .leadStatus(dto.getLeadStatus())
                .partnershipTier(dto.getPartnershipTier())
                .submittedDate(dto.getSubmittedDate())
                .build();
    }

    private void updateEntity(PartnerLead entity, PartnerLeadDTO dto) {
        entity.setUserId(dto.getUserId());

        entity.setCompanyName(dto.getCompanyName());
        entity.setCompanyWebsite(dto.getCompanyWebsite());
        entity.setIndustry(dto.getIndustry());
        entity.setCompanySize(dto.getCompanySize());
        entity.setGeography(dto.getGeography());
        entity.setEstimatedAcv(dto.getEstimatedAcv());

        entity.setContactName(dto.getContactName());
        entity.setContactTitle(dto.getContactTitle());
        entity.setContactEmail(dto.getContactEmail());
        entity.setContactLinkedIn(dto.getContactLinkedIn());
        entity.setBuyingIntentSignal(dto.getBuyingIntentSignal());

        entity.setLeadTemperature(dto.getLeadTemperature());
        entity.setInvolvementLevel(dto.getInvolvementLevel());

        entity.setPreferredMeetingFormat(dto.getPreferredMeetingFormat());
        entity.setMessageToSharkdomTeam(dto.getMessageToSharkdomTeam());

        entity.setEstimatedCommission(dto.getEstimatedCommission());
        entity.setRate(dto.getRate());

        entity.setLeadStatus(dto.getLeadStatus());
        entity.setPartnershipTier(dto.getPartnershipTier());

        entity.setPaymentDate(dto.getPaymentDate());
        entity.setPaymentStatus(dto.getPaymentStatus());
        entity.setInvoiceLink(dto.getInvoiceLink());

        entity.setAssignedAe(dto.getAssignedAe());
    }

    private PartnerLeadDTO mapToDTO(PartnerLead entity) {
        PartnerLeadDTO dto = new PartnerLeadDTO();

        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());

        dto.setCompanyName(entity.getCompanyName());
        dto.setCompanyWebsite(entity.getCompanyWebsite());
        dto.setIndustry(entity.getIndustry());
        dto.setCompanySize(entity.getCompanySize());
        dto.setGeography(entity.getGeography());
        dto.setEstimatedAcv(entity.getEstimatedAcv());

        dto.setContactName(entity.getContactName());
        dto.setContactTitle(entity.getContactTitle());
        dto.setContactEmail(entity.getContactEmail());
        dto.setContactLinkedIn(entity.getContactLinkedIn());
        dto.setBuyingIntentSignal(entity.getBuyingIntentSignal());

        dto.setLeadTemperature(entity.getLeadTemperature());
        dto.setInvolvementLevel(entity.getInvolvementLevel());

        dto.setPreferredMeetingFormat(entity.getPreferredMeetingFormat());
        dto.setMessageToSharkdomTeam(entity.getMessageToSharkdomTeam());

        dto.setEstimatedCommission(entity.getEstimatedCommission());
        dto.setRate(entity.getRate());

        dto.setLeadStatus(entity.getLeadStatus());
        dto.setPartnershipTier(entity.getPartnershipTier());

        dto.setPaymentDate(entity.getPaymentDate());
        dto.setPaymentStatus(entity.getPaymentStatus());
        dto.setInvoiceLink(entity.getInvoiceLink());

        dto.setAssignedAe(entity.getAssignedAe());

        return dto;
    }

    /**
     * Maps a PartnerLead entity to a PartnerLeadDTO with all UI-facing display fields populated.
     * Used by the Recent Leads endpoint so the frontend can render badges, action buttons,
     * and commission display text without any additional client-side logic.
     */
    private PartnerLeadDTO mapToRecentLeadDTO(PartnerLead entity) {
        PartnerLeadDTO dto = mapToDTO(entity);

        // ── Tier display label ────────────────────────────────────────────────
        if (entity.getPartnershipTier() != null) {
            dto.setTierDisplay(formatTierDisplay(entity.getPartnershipTier()));
        } else if (entity.getInvolvementLevel() != null) {
            dto.setTierDisplay(formatTierDisplay(entity.getInvolvementLevel()));
        }

        // ── Status label (human-readable badge text) ──────────────────────────
        if (entity.getLeadStatus() != null) {
            dto.setStatusLabel(formatStatusLabel(entity.getLeadStatus()));
        }

        // ── Estimated commission display range text ───────────────────────────
        if (entity.getEstimatedCommissionDisplay() != null) {
            dto.setEstimatedCommissionDisplay(entity.getEstimatedCommissionDisplay());
        } else if (entity.getEstimatedCommission() != null) {
            // Derive a simple range: commission ± 25 %
            java.math.BigDecimal base = entity.getEstimatedCommission();
            java.math.BigDecimal low  = base.multiply(new java.math.BigDecimal("0.75"))
                    .setScale(0, java.math.RoundingMode.HALF_UP);
            java.math.BigDecimal high = base.multiply(new java.math.BigDecimal("1.25"))
                    .setScale(0, java.math.RoundingMode.HALF_UP);
            dto.setEstimatedCommissionDisplay(
                    String.format("$%,.0f - $%,.0f", low.doubleValue(), high.doubleValue()));
        }

        // ── Per-status action label, url and UI permission flags ──────────────
        populateActionFields(dto, entity);

        return dto;
    }

    private String formatTierDisplay(com.sharkdom.partnerprogram.enums.PartnershipTier tier) {
        if (tier == null) return null;
        return switch (tier) {
            case CHAMPION_PARTNER -> "CHAMPION PARTNER";
            case REFERRAL_PARTNER -> "REFERRAL PARTNER";
        };
    }

    private String formatStatusLabel(LeadStatus status) {
        return switch (status) {
            case DEMO_SCHEDULED -> "DEMO SCHEDULED";
            case ACCEPTED       -> "ACCEPTED";
            case DECLINED       -> "DECLINED";
            case ONBOARDED      -> "ONBOARDED";
            case UNDER_REVIEW   -> "UNDER REVIEW";
            case SUBMITTED      -> "SUBMITTED";
            case CONTACTED      -> "CONTACTED";
            case QUALIFIED      -> "QUALIFIED";
            case WON            -> "WON";
            case LOST           -> "LOST";
        };
    }

    /**
     * Derives the action button label, redirect URL, and permission flags
     * based on the current lead status.
     *
     * <ul>
     *   <li>DEMO_SCHEDULED → "View Details"</li>
     *   <li>ACCEPTED       → "View Progress"</li>
     *   <li>DECLINED       → "Resubmit"</li>
     *   <li>ONBOARDED      → "View Commission"</li>
     *   <li>UNDER_REVIEW   → canCancel = true</li>
     *   <li>others         → "View Details"</li>
     * </ul>
     */
    private void populateActionFields(PartnerLeadDTO dto, PartnerLead entity) {
        if (entity.getLeadStatus() == null) return;

        // Prefer stored values from entity when already set
        if (entity.getActionLabel() != null) {
            dto.setActionLabel(entity.getActionLabel());
            dto.setActionUrl(entity.getActionUrl());
        }

        switch (entity.getLeadStatus()) {
            case DEMO_SCHEDULED -> {
                dto.setActionLabel(dto.getActionLabel() != null ? dto.getActionLabel() : "View Details");
                dto.setCanViewDetails(true);
                dto.setCanResubmit(false);
                dto.setCanCancel(false);
                dto.setCanViewCommission(false);
                dto.setCanViewProgress(false);
            }
            case ACCEPTED -> {
                dto.setActionLabel(dto.getActionLabel() != null ? dto.getActionLabel() : "View Progress");
                dto.setCanViewProgress(true);
                dto.setCanResubmit(false);
                dto.setCanCancel(false);
                dto.setCanViewCommission(false);
                dto.setCanViewDetails(true);
            }
            case DECLINED -> {
                dto.setActionLabel(dto.getActionLabel() != null ? dto.getActionLabel() : "Resubmit");
                dto.setCanResubmit(true);
                dto.setCanCancel(false);
                dto.setCanViewCommission(false);
                dto.setCanViewProgress(false);
                dto.setCanViewDetails(true);
            }
            case ONBOARDED, WON -> {
                dto.setActionLabel(dto.getActionLabel() != null ? dto.getActionLabel() : "View Commission");
                dto.setCanViewCommission(true);
                dto.setCanResubmit(false);
                dto.setCanCancel(false);
                dto.setCanViewProgress(false);
                dto.setCanViewDetails(true);
            }
            case UNDER_REVIEW, SUBMITTED, CONTACTED, QUALIFIED -> {
                dto.setCanCancel(true);
                dto.setCanResubmit(false);
                dto.setCanViewCommission(false);
                dto.setCanViewProgress(false);
                dto.setCanViewDetails(true);
            }
            default -> {
                dto.setActionLabel(dto.getActionLabel() != null ? dto.getActionLabel() : "View Details");
                dto.setCanViewDetails(true);
            }
        }
    }

    public PartnerCommissionStatsDTO getCommissionStats(String userId) {

        return PartnerCommissionStatsDTO.builder()
                .totalEarned(0.00)
                .pendingCommission(0.00)
                .nextPayoutDate("May 1")
                .build();
    }


    @Override
    public SharkdomPaginatedResponse<PartnerCommissionDTO> getCommissions(
            String userId,
            int page,
            int size
    ) {

        Page<PartnerLead> partnerLeads = repository.findByUserId(
                userId,
                PageRequest.of(page, size, Sort.by("id").descending())
        );

        SharkdomPaginatedResponse<PartnerCommissionDTO> response =
                new SharkdomPaginatedResponse<>();

        response.setContent(
                partnerLeads.getContent()
                        .stream()
                        .map(this::mapToCommissionDTO)
                        .toList()
        );

        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(partnerLeads.getTotalElements());
        response.setTotalPages(partnerLeads.getTotalPages());
        response.setLast(partnerLeads.isLast());

        return response;
    }

    private PartnerCommissionDTO mapToCommissionDTO(PartnerLead entity) {
        return PartnerCommissionDTO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .companyName(entity.getCompanyName())
                .partnershipTier(entity.getPartnershipTier())
                .dealAcv(entity.getEstimatedAcv())
                .rate(entity.getRate())
                .commission(entity.getEstimatedCommission())
                .paymentStatus(entity.getPaymentStatus())
                .paymentDate(entity.getPaymentDate())
                .invoiceUrl(entity.getInvoiceLink())
                .assignedAe(entity.getAssignedAe())
                .build();
    }


}

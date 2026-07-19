package com.sharkdom.partnerprogram.service;

import com.sharkdom.partnerprogram.dtos.PartnerCommissionDTO;
import com.sharkdom.partnerprogram.dtos.PartnerCommissionStatsDTO;
import com.sharkdom.partnerprogram.dtos.PartnerLeadDTO;
import com.sharkdom.util.SharkdomPaginatedResponse;

import java.util.List;

public interface PartnerLeadService {

    PartnerLeadDTO create(PartnerLeadDTO dto);

    PartnerLeadDTO update(Long id, PartnerLeadDTO dto);

    void delete(Long id);

    PartnerLeadDTO getById(Long id);

    SharkdomPaginatedResponse<PartnerLeadDTO> getAll(String userId, int page, int size);

    /**
     * Returns the most recently submitted leads for a partner, sorted by submittedDate DESC.
     * Each lead item includes all UI display fields: statusLabel, actionLabel, tierDisplay,
     * estimatedCommissionDisplay, and canXxx permission flags.
     */
    SharkdomPaginatedResponse<PartnerLeadDTO> getRecentLeads(String userId, int page, int size);

    public SharkdomPaginatedResponse<PartnerCommissionDTO> getCommissions(
            String userId,
            int page,
            int size
    );

    PartnerCommissionStatsDTO getCommissionStats(String userId);
}
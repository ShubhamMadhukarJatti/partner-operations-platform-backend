package com.sharkdom.partnerprogram.service;

import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.user.User;
import com.sharkdom.partnerprogram.dtos.AskForPaymentRequestDTO;
import com.sharkdom.partnerprogram.dtos.PartnerApplicationDTO;
import com.sharkdom.partnerprogram.dtos.PartnerDashboardStatsDTO;
import com.sharkdom.partnerprogram.dtos.UpdateReferralCodeRequest;
import com.sharkdom.partnerprogram.dtos.SetPasswordRequest;
import com.sharkdom.util.SharkdomPaginatedResponse;

public interface PartnerApplicationService {

    PartnerApplicationDTO create(PartnerApplicationDTO dto);

    PartnerApplicationDTO update(Long id, PartnerApplicationDTO dto);

    void delete(Long id);

    PartnerApplicationDTO getById(Long id);

    SharkdomPaginatedResponse<PartnerApplicationDTO> getAll(int page, int size);

    public void disablePartner(String email);

    public User approvePartner(String email);

    public void enablePartner(String email);

    PartnerDashboardStatsDTO getDashboardStats(String userId);

    PartnerApplicationDTO getByUserId(String userId);

    PartnerApplicationDTO getByEmail(String email);

    PartnerApplicationDTO updateByUserId(String userId, PartnerApplicationDTO dto);

    public Organization updateReferralCodeByEmail(UpdateReferralCodeRequest request);

    public void askForPayment(AskForPaymentRequestDTO request);

    public void setPassword(String email, SetPasswordRequest request);

}

package com.sharkdom.partnerprogram.service;

import com.sharkdom.partnerprogram.dtos.CompanyPartnerApplicationDTO;
import com.sharkdom.util.SharkdomPaginatedResponse;

public interface CompanyPartnerApplicationService {

    CompanyPartnerApplicationDTO create(CompanyPartnerApplicationDTO dto);

    CompanyPartnerApplicationDTO update(Long id, CompanyPartnerApplicationDTO dto);

    void delete(Long id);

    CompanyPartnerApplicationDTO getById(Long id);

    SharkdomPaginatedResponse<CompanyPartnerApplicationDTO> getAll(int page, int size);
}
package com.sharkdom.partnerprogram.service;

import com.sharkdom.partnerprogram.dtos.ConsultantPartnerApplicationDTO;
import com.sharkdom.util.SharkdomPaginatedResponse;

public interface ConsultantPartnerApplicationService {

    ConsultantPartnerApplicationDTO create(ConsultantPartnerApplicationDTO dto);

    ConsultantPartnerApplicationDTO update(Long id, ConsultantPartnerApplicationDTO dto);

    void delete(Long id);

    ConsultantPartnerApplicationDTO getById(Long id);

    SharkdomPaginatedResponse<ConsultantPartnerApplicationDTO> getAll(int page, int size);
}
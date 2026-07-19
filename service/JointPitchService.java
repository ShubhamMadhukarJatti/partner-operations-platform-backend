package com.sharkdom.partnerattribution.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.partnerattribution.dto.JointPitchRequestDTO;
import com.sharkdom.partnerattribution.dto.JointPitchResponseDTO;
import com.sharkdom.partnerattribution.entities.JointPitch;
import com.sharkdom.partnerattribution.repository.JointPitchRepository;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JointPitchService {

    private final JointPitchRepository repository;

    public JointPitchResponseDTO saveOrUpdate(JointPitchRequestDTO request) {

        Long orgId = Util.getOrgIdFromToken();

        log.info("Saving JointPitch | orgId={} | partnerOrgId={}",
                orgId, request.getPartnerOrgId());

        JointPitch pitch = repository
                .findByOrgIdAndPartnerOrgIdAndDealIdAndIsDeletedFalse(orgId, request.getPartnerOrgId(),request.getDealId())
                .orElse(new JointPitch());

        pitch.setOrgId(orgId);
        pitch.setDealId(request.getDealId());
        pitch.setPartnerOrgId(request.getPartnerOrgId());
        pitch.setPitch(request.getPitch());

        JointPitch saved = repository.save(pitch);

        return mapToDto(saved);
    }

    public JointPitchResponseDTO get(Long partnerOrgId,String dealId) {

        Long orgId = Util.getOrgIdFromToken();

        log.info("Fetching JointPitch | orgId={} | partnerOrgId={}",
                orgId, partnerOrgId);

        JointPitch pitch = repository
                .findByOrgIdAndPartnerOrgIdAndDealIdAndIsDeletedFalse(orgId, partnerOrgId,dealId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.NOT_FOUND));

        return mapToDto(pitch);
    }

    private JointPitchResponseDTO mapToDto(JointPitch entity) {
        JointPitchResponseDTO dto = new JointPitchResponseDTO();
        dto.setId(entity.getId());
        dto.setOrgId(entity.getOrgId());
        dto.setDealId(entity.getDealId());
        dto.setPartnerOrgId(entity.getPartnerOrgId());
        dto.setPitch(entity.getPitch());
        dto.setLastEditedBy(entity.getLastEditedBy());
        dto.setLastEditedAt(entity.getLastEditedAt());
        return dto;
    }
}
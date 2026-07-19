package com.sharkdom.offlinePartner.service;

import com.sharkdom.dto.SaveAssignmentDto;
import com.sharkdom.entity.externalpartner.ExternalPartnerAssignment;
import com.sharkdom.entity.mypartner.MyPartnerAssignment;
import com.sharkdom.repository.externalpartner.ExternalPartnerAssignmentRepository;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExternalPartnerAssignmentService {

    @Autowired
    private ExternalPartnerAssignmentRepository externalPartnerAssignmentRepository;

    @Autowired
    private UserRepository userRepository;


    public ExternalPartnerAssignment saveExternalPartnerAssignment(SaveAssignmentDto saveAssignmentDto) {
        Long orgId = Util.getOrgIdFromToken();
        Long partnerOrgId = saveAssignmentDto.getPartnerOrgId();
        String userId = saveAssignmentDto.getUserId();

        log.info("Saving ExternalPartnerAssignment - orgId: {}, partnerOrgId: {}, userId: {}",orgId, partnerOrgId, userId);

        // Check existing record (Upsert logic)
        ExternalPartnerAssignment existingAssignment = externalPartnerAssignmentRepository
                .findByOrganizationIdAndExternalPartnerId(orgId, partnerOrgId)
                .orElse(null);

        if (existingAssignment != null) {
            log.info("Existing assignment found for orgId: {} and partnerOrgId: {}. Updating userId to: {}", orgId, partnerOrgId, userId);
            existingAssignment.setUserId(userId);
            return externalPartnerAssignmentRepository.save(existingAssignment);
        }

        log.info("No existing assignment found. Creating new assignment for orgId: {} and partnerOrgId: {}", orgId, partnerOrgId);

        ExternalPartnerAssignment newAssignment = new ExternalPartnerAssignment();
        newAssignment.setOrganizationId(orgId);
        newAssignment.setExternalPartnerId(partnerOrgId);
        newAssignment.setUserId(userId);

        return externalPartnerAssignmentRepository.save(newAssignment);
    }

    public ExternalPartnerAssignment getMyPartnerAssignment(Long partnerId) {
        Long orgId = Util.getOrgIdFromToken();
        log.info("Retrieving MyPartnerAssignment - orgId: {}, partnerId: {}", orgId, partnerId);

        return externalPartnerAssignmentRepository
                .findByOrganizationIdAndExternalPartnerId(orgId, partnerId)
                .map(epa -> {
                    ExternalPartnerAssignment mpa = new ExternalPartnerAssignment();
                    mpa.setOrganizationId(epa.getOrganizationId());
                    mpa.setExternalPartnerId(epa.getExternalPartnerId());
                    mpa.setUserId(epa.getUserId());
                    mpa.setId(epa.getId());
                    mpa.setCreationTimestamp(epa.getCreationTimestamp());
                    mpa.setLastUpdatedTimestamp(epa.getLastUpdatedTimestamp());
                    mpa.setUsername(userRepository.findByUserId(epa.getUserId()).get().getName());
                    return mpa;
                })
                .orElse(null);
    }


}

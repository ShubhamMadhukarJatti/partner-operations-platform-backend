package com.sharkdom.service.organizationcollaboration;

import com.sharkdom.dto.OrganizationCollaborationGroupingDataCountDTO;
import com.sharkdom.dto.OrganizationPartnerCategoryResponse;
import com.sharkdom.dto.OrganizationPartnerResponse;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organizationcollaboration.OrganizationCollaboration;
import com.sharkdom.entity.organizationcollaboration.OrganizationCollaborationCategoryEntity;
import com.sharkdom.model.organizatiocollaboration.CollaborationCategory;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organizationcollaboration.OrganizationCollaborationCategoryRepository;
import com.sharkdom.repository.organizationcollaboration.OrganizationCollaborationRepository;
import com.sharkdom.service.ai.PersonaService;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class OrganizationCollaborationGroupService {

    @Autowired
    private OrganizationCollaborationCategoryRepository organizationCollaborationCategoryRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationCollaborationRepository organizationCollaborationRepository;

    @Autowired
    private PersonaService personaService;


    public OrganizationCollaborationGroupingDataCountDTO getOrganizationCollaborationGroupingDataCount() {
        Long orgIdFromToken = Util.getOrgIdFromToken();
        log.info("Starting to fetch Organization Collaboration Grouping Data Count for organizationId: {}", orgIdFromToken);

        if (orgIdFromToken == null) {
            log.warn("Organization ID not found in token. Returning empty DTO.");
            return new OrganizationCollaborationGroupingDataCountDTO();
        }

        OrganizationCollaborationGroupingDataCountDTO dto = new OrganizationCollaborationGroupingDataCountDTO();

        try {
            // Reliable Partners
            Long reliablePartnersCount = Optional.ofNullable(
                    organizationCollaborationCategoryRepository.findByOrganizationIdAndCategory(orgIdFromToken, CollaborationCategory.RELIABLE_PARTNER)
            ).map(List::size).map(Long::valueOf).orElse(0L);
            dto.setReliablePartnersCount(reliablePartnersCount);
            log.info("Reliable Partners Count: {}", reliablePartnersCount);

            // Steady Partners
            Long steadyPartnersCount = Optional.ofNullable(
                    organizationCollaborationCategoryRepository.findByOrganizationIdAndCategory(orgIdFromToken, CollaborationCategory.STEADY_PARTNER)
            ).map(List::size).map(Long::valueOf).orElse(0L);
            dto.setSteadyPartnersCount(steadyPartnersCount);
            log.info("Steady Partners Count: {}", steadyPartnersCount);

            // Low Impact Partners
            Long lowImpactPartnersCount = Optional.ofNullable(
                    organizationCollaborationCategoryRepository.findByOrganizationIdAndCategory(orgIdFromToken, CollaborationCategory.LOW_IMPACT_PARTNER)
            ).map(List::size).map(Long::valueOf).orElse(0L);
            dto.setLowImpactPartnersCount(lowImpactPartnersCount);
            log.info("Low Impact Partners Count: {}", lowImpactPartnersCount);

            // Inactive Partners
            Long inactivePartnersCount = Optional.ofNullable(
                    organizationCollaborationCategoryRepository.findByOrganizationIdAndCategory(orgIdFromToken, CollaborationCategory.INACTIVE_PARTNER)
            ).map(List::size).map(Long::valueOf).orElse(0L);
            dto.setInactivePartnersCount(inactivePartnersCount);
            log.info("Inactive Partners Count: {}", inactivePartnersCount);

            // Total Partners
            Long totalPartnersCount = Optional.ofNullable(
                    organizationCollaborationRepository.findAllActiveCollaborationBySenderOrganizationId(orgIdFromToken)
            ).map(List::size).map(Long::valueOf).orElse(0L);
            dto.setTotalPartnersCount(totalPartnersCount);
            log.info("Total Active Partners Count: {}", totalPartnersCount);

            log.info("Successfully fetched collaboration grouping data for orgId: {}", orgIdFromToken);
        } catch (Exception e) {
            log.error("Error while fetching collaboration grouping data for orgId: {} | Error: {}", orgIdFromToken, e.getMessage(), e);
        }

        return dto;
    }

    public List<OrganizationPartnerCategoryResponse> getPartnersByAllCategories() {
        log.info("Fetching partners grouped by all categories");

        Long organizationId = Util.getOrgIdFromToken();
        log.debug("Extracted organizationId from token: {}", organizationId);

        List<OrganizationPartnerCategoryResponse> groupedResponses = new ArrayList<>();

        for (CollaborationCategory category : CollaborationCategory.values()) {
            log.info("Processing category: {}", category);

            List<OrganizationCollaborationCategoryEntity> collaborationCategories =
                    organizationCollaborationCategoryRepository.findByOrganizationIdAndCategory(organizationId, category);

            log.info("Found {} collaborations for category: {}", collaborationCategories.size(), category);

            List<OrganizationPartnerResponse> partnerResponses = new ArrayList<>();

            for (OrganizationCollaborationCategoryEntity c : collaborationCategories) {
                Long collaborationId = c.getOrganizationCollaborationId();
                log.debug("Processing collaborationId: {}", collaborationId);

                Optional<OrganizationCollaboration> optionalCollab =
                        organizationCollaborationRepository.findById(collaborationId);

                if (optionalCollab.isEmpty()) {
                    log.warn("No OrganizationCollaboration found for collaborationId: {}", collaborationId);
                    continue;
                }

                long receiverOrganizationId = optionalCollab.get().getReceiverOrganizationId();
                log.debug("Receiver organization ID: {}", receiverOrganizationId);

                Optional<Organization> optionalOrg = organizationRepository.findById(receiverOrganizationId);
                if (optionalOrg.isEmpty()) {
                    log.warn("No Organization found for receiverOrganizationId: {}", receiverOrganizationId);
                    continue;
                }

                Organization org = optionalOrg.get();
                OrganizationPartnerResponse partnerResponse = new OrganizationPartnerResponse();
                partnerResponse.setOrganizationId(org.getId());
                partnerResponse.setLogoUrl(org.getLogoUrl());
                partnerResponse.setAssignmentDate(c.getCreationTimestamp());
                partnerResponse.setOrganizationName(org.getName());
                partnerResponse.setPartnerDataPermissionResponse(
                        personaService.getPartnerDataPermissions(organizationId)
                );

                partnerResponses.add(partnerResponse);
                log.info("Added partner: {} (ID: {}) under category {}", org.getName(), org.getId(), category);
            }

            groupedResponses.add(new OrganizationPartnerCategoryResponse(category, partnerResponses));
        }

        log.info("Completed grouping partners by all categories. Total groups: {}", groupedResponses.size());
        return groupedResponses;
    }

}

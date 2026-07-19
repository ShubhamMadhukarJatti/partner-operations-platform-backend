package com.sharkdom.AIpartnerPulse.service;

import com.sharkdom.constants.organization.OrganizationStatus;
import com.sharkdom.entity.organization.*;
import com.sharkdom.profilesection.service.OrganizationCertificationService;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationSearchService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationCertificationService organizationCertificationService;

    public int calculateMatchScore(Organization org1, Organization org2) {

        int score = 0;

        if (org1 == null || org2 == null) {
            return 0;
        }

        // ---------- FILTER MATCH ----------
        List<String> filters1 = org1.getFilters();
        List<String> filters2 = org2.getFilters();

        if (filters1 != null && filters2 != null && !filters1.isEmpty() && !filters2.isEmpty()) {

            Set<String> filterSet = new HashSet<>(filters1);

            for (String filter : filters2) {
                if (filterSet.contains(filter)) {
                    score += 80;
                    break;
                }
            }
        }

        // ---------- TEAM SIZE MATCH ----------
        TeamSize teamSize1 = org1.getPartnershipTeamSize();
        TeamSize teamSize2 = org2.getPartnershipTeamSize();

        if (teamSize1 != null && teamSize2 != null) {

            if (teamSize1 != TeamSize.ZERO && teamSize2 != TeamSize.ZERO) {
                score += 20;
            }
        }

        return score;
    }

    public Page<OrganizationCustomResponse> searchByFilter(
            List<String> filters,
            List<String> sectors,
            List<String> partnershipTypes,
            String keyword,
            String sectorType,
            int page,
            int size) {

        long start = System.currentTimeMillis();

        log.info("[searchByFilter] Search initiated | filters={} | sectors={} | partnerships={} | keyword={} | sectorType={} | page={} | size={}",
                filters, sectors, partnershipTypes, keyword, sectorType, page, size);

        Specification<Organization> spec = Specification
                .where(OrganizationSpecifications.hasStatus(OrganizationStatus.ACTIVE))
                .and(OrganizationSpecifications.hasFilterIn(filters))
                .and(OrganizationSpecifications.hasPreferredSectorIn(sectors))
                .and(OrganizationSpecifications.hasPreferredPartnershipIn(partnershipTypes))
                .and(OrganizationSpecifications.hasKeywordInMultipleFields(keyword))
                .and(OrganizationSpecifications.hasSectorType(sectorType))
                .and(OrganizationSpecifications.hasValidLogoUrl())
                .and(OrganizationSpecifications.hasAtLeastOnePartnerTier());

        Page<Organization> result = organizationRepository.findAll(
                spec,
                PageRequest.of(page, size)
        );

        long dbEnd = System.currentTimeMillis();
        log.info("[searchByFilter] DB Query Completed | found={} | timeTaken={}ms",
                result.getTotalElements(), (dbEnd - start));
        var optOrg = organizationRepository.findById(Util.getOrgIdFromToken());

        Page<OrganizationCustomResponse> dtoPage = result.map(org -> new OrganizationCustomResponse(
                org.getId(),
                org.getName(),
                org.getAbout(),
                org.getBriefDescription(),
                org.getWebsite(),
                org.getLogoUrl(),
                org.getMeetingSuccessRate(),
                org.getAcknowledgmentTime(),
                org.getActivePartnerships(),
                org.getPipelinePartnerships(),
                org.getLegalName(),
                org.getPreferredSectors() != null ?
                        org.getPreferredSectors().stream().map(PreferredSector::getArea).toList() :
                        Collections.emptyList(),
                org.getFilters(),
                org.getPrimaryEmail(),
                org.isSelectedForExternalPartnerships(),
                org.isShortlisted(),
                org.getCompliances(),
                calculateMatchScore(org,optOrg.get()),
                org.getTrustpilotRating(),
                org.getTrustpilotTotalReviews()
        ));

        long end = System.currentTimeMillis();
        log.info("[searchByFilter] DTO Mapping Completed | finalCount={} | totalTime={}ms",
                dtoPage.getTotalElements(), (end - start));

        return dtoPage;
    }
}

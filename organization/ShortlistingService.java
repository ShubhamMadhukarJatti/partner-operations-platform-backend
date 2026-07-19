package com.sharkdom.service.organization;

import com.sharkdom.constants.organization.OrganizationStatus;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organization.ShortlistOrganization;
import com.sharkdom.entity.organization.ShortlistedOrganizationResponse;
import com.sharkdom.model.organization.OrganizationResponse;
import com.sharkdom.model.organization.OrganizationSearchResponse;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organization.ShortlistOrganizationRepository;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShortlistingService {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ShortlistOrganizationRepository shortlistOrganizationRepository;

    public ShortlistOrganization saveShortListOrganization(ShortlistOrganization shortlistOrganization) {
        log.info("Saving shortlist organization: shortlistedOrgId={}, shortlistedByOrgId={}, shortlistedByUserId={}",
                shortlistOrganization.getShortlistedOrgId(),
                shortlistOrganization.getShortlistedByOrgId(),
                shortlistOrganization.getShortlistedByUserId(),
                shortlistOrganization.getShortlistedByUserName());
        try {
            var shortlisted = shortlistOrganizationRepository.findByShortlistedByOrgIdAndShortlistedOrgId(shortlistOrganization.getShortlistedByOrgId(), shortlistOrganization.getShortlistedOrgId());
            if (shortlisted.isEmpty()) {
                ShortlistOrganization saved = shortlistOrganizationRepository.save(shortlistOrganization);
                log.info("Successfully saved shortlist organization with id={}", saved.getId());
                return saved;
            }
        } catch (Exception e) {
            log.error("Failed to save shortlist organization: shortlistedOrgId={}, shortlistedByOrgId={}, shortlistedByUserId={}. Error: {}",
                    shortlistOrganization.getShortlistedOrgId(),
                    shortlistOrganization.getShortlistedByOrgId(),
                    shortlistOrganization.getShortlistedByUserId(),
                    shortlistOrganization.getShortlistedByUserName(),
                    e.getMessage(), e);
            throw new RuntimeException("Unable to save shortlist organization", e);
        }
        return shortlistOrganization;
    }

    public List<ShortlistedOrganizationResponse> getShortListedOrganizations(String orgId, int page, int size) {
        log.info("Fetching shortlisted organizations for orgId: {}, page: {}, size: {}", orgId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("creationTimestamp").descending());
        Page<ShortlistOrganization> shortlistPage =
                shortlistOrganizationRepository.findByShortlistedByOrgId(Long.parseLong(orgId), pageable);

        if (shortlistPage.isEmpty()) {
            log.warn("No shortlisted organizations found for orgId: {}", orgId);
            return Collections.emptyList();
        }

        List<ShortlistOrganization> shortlistContent = shortlistPage.getContent();

        // Extract shortlisted org IDs
        List<Long> shortlistedOrgIds = shortlistContent.stream()
                .map(ShortlistOrganization::getShortlistedOrgId)
                .distinct()
                .toList();

        log.debug("Extracted shortlisted org IDs: {}", shortlistedOrgIds);

        // Fetch organizations in one go
        List<Organization> organizations =
                organizationRepository.findByIdIn(shortlistedOrgIds);

        log.info("Fetched {} organizations for shortlistedOrgIds: {}",
                organizations.size(), shortlistedOrgIds.stream().map(String::valueOf).toList());

        // Optional: log organization details (ids + names)
        organizations.forEach(org ->
                log.debug("Organization fetched -> id: {}, name: {}", org.getId(), org.getName()));

        Map<Long, Organization> orgMap =
                organizations.stream().collect(Collectors.toMap(Organization::getId, o -> o));

        log.info("Fetched {} active organizations from repository", organizations.size());

        // Merge shortlist + organization data
        List<ShortlistedOrganizationResponse> responses = shortlistContent.stream()
                .map(shortlist -> {
                    Organization org = orgMap.get(shortlist.getShortlistedOrgId());
                    if (org == null) {
                        log.warn("No organization details found for shortlistedOrgId={}", shortlist.getShortlistedOrgId());
                        return null;
                    }

                    // Build final response
                    return ShortlistedOrganizationResponse.builder()
                            .shortlistedOrgId(shortlist.getShortlistedOrgId())
                            .shortlistedByUserId(shortlist.getShortlistedByUserId())
                            .shortlistedByUserName(shortlist.getShortlistedByUserName())
                            .shortlistedByOrgId(shortlist.getShortlistedByOrgId())
                            .remark(shortlist.getRemark())
                            .creationTimestamp(shortlist.getCreationTimestamp())
                            .logoUrl(org.getLogoUrl())
                            .name(org.getName())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        log.info("Returning {} shortlisted organization responses for orgId={}", responses.size(), orgId);
        return responses;
    }

    public boolean isOrganizationShortlisted(Long shortlistedOrgId) {
        Long shortlistedByOrgId= Util.getOrgIdFromToken();
        return shortlistOrganizationRepository
                .findByShortlistedByOrgIdAndShortlistedOrgId(shortlistedByOrgId, shortlistedOrgId)
                .isPresent();
    }

    @Transactional
    public void removeShortlistedOrganization(Long shortlistedOrgId) {

        Long shortlistedByOrgId = Util.getOrgIdFromToken();

        log.info("Removing shortlisted organization: shortlistedOrgId={}, shortlistedByOrgId={}",
                shortlistedOrgId, shortlistedByOrgId);

        Optional<ShortlistOrganization> existing =
                shortlistOrganizationRepository
                        .findByShortlistedByOrgIdAndShortlistedOrgId(shortlistedByOrgId, shortlistedOrgId);

        if (existing.isEmpty()) {
            log.warn("Shortlist not found for shortlistedOrgId={} and shortlistedByOrgId={}",
                    shortlistedOrgId, shortlistedByOrgId);
            throw new RuntimeException("Shortlisted organization not found");
        }

        shortlistOrganizationRepository
                .deleteByShortlistedByOrgIdAndShortlistedOrgId(shortlistedByOrgId, shortlistedOrgId);

        log.info("Successfully removed shortlist for shortlistedOrgId={} by orgId={}",
                shortlistedOrgId, shortlistedByOrgId);
    }



}

package com.sharkdom.profilesection.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.dto.PartnerPortalBrandingResponse;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.ppi.CounterSave;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.model.ppi.CounterStatsResponse;
import com.sharkdom.partnertraining.dto.LabelResponse;
import com.sharkdom.partnertraining.service.LabelService;
import com.sharkdom.profilesection.dto.*;
import com.sharkdom.profilesection.entity.OrganizationProfileCompletion;
import com.sharkdom.profilesection.entity.OrganizationSearchCounter;
import com.sharkdom.profilesection.entity.OrganizationSeniorityCounter;
import com.sharkdom.profilesection.repository.OrganizationProfileCompletionRepository;
import com.sharkdom.profilesection.repository.OrganizationSearchCounterRepository;
import com.sharkdom.profilesection.repository.OrganizationSeniorityCounterRepository;
import com.sharkdom.repository.catalogue.PartnerTierRepository;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organizationcollaboration.OrganizationCollaborationRepository;
import com.sharkdom.repository.ppi.CounterSaveRepository;
import com.sharkdom.repository.ppi.PartnerPortalBrandingRepository;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerOrganizationService {

    private final LabelService labelService;
    private final MarketSegmentService marketSegmentService;
    private final OrganizationSearchCounterRepository searchCounterRepository;
    private final OrganizationRepository organizationRepository;
    private final IntegrationRepository integrationRepository;
    private final OrganizationProfileCompletionRepository organizationProfileCompletionRepository;
    private final CounterSaveRepository counterSaveRepository;
    private final PartnerPortalBrandingRepository partnerPortalBrandingRepository;
    private final PartnerTierRepository partnerTierRepository;
    private final OrganizationSeniorityCounterRepository seniorityCounterRepository;
    private final OrganizationCollaborationRepository organizationCollaborationRepository;

    /**
     * Increment search count (Write Operation)
     */
    @Transactional
    public Integer incrementSearchCount(Long orgId) {

        log.info("[INCREMENT_SEARCH_COUNT] orgId={}", orgId);

        OrganizationSearchCounter counter = searchCounterRepository
                .findByOrganizationId(orgId)
                .orElseGet(() -> buildNewCounter(orgId));

        counter.setSearchCount(counter.getSearchCount() + 1);

        searchCounterRepository.save(counter);

        log.info("[SEARCH_COUNT_UPDATED] orgId={} count={}", orgId, counter.getSearchCount());

        return counter.getSearchCount();
    }

    /**
     * Get search count (Read Operation)
     */
    @Transactional(readOnly = true)
    public OrganizationSearchCounterResponse getByOrganizationId(Long orgId) {

        log.info("[GET_SEARCH_COUNT] orgId={}", orgId);

        return searchCounterRepository.findByOrganizationId(orgId)
                .map(counter -> OrganizationSearchCounterResponse.builder()
                        .organizationId(counter.getOrganizationId())
                        .searchCount(counter.getSearchCount())
                        .build())
                .orElseGet(() -> OrganizationSearchCounterResponse.builder()
                        .organizationId(orgId)
                        .searchCount(0)
                        .build());
    }

    /**
     * Get complete Partner Organization Details
     */
    @Transactional(readOnly = true)
    public PartnerOrganizationResponse getPartnerOrganizationDetails(Long orgId) {

        log.info("[GET_PARTNER_ORG_DETAILS] orgId={}", orgId);

        // 1. Fetch Organization
        var organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> {
                    log.error("[ORG_NOT_FOUND] orgId={}", orgId);
                    return new ServiceException(ErrorMessages.SH75, orgId);
                });

        // 2. Fetch Search Count (single DB call)
        int searchCount = searchCounterRepository.findByOrganizationId(orgId)
                .map(OrganizationSearchCounter::getSearchCount)
                .orElse(0);

        // 3. Fetch Labels
        List<LabelResponse> labels = fetchLabelsSafely(orgId);

        // 4. Fetch Market Segment
        List<String> marketSegment = fetchMarketSegmentSafely(orgId, organization.getWebsite());

        return PartnerOrganizationResponse.builder()
                .organizationId(orgId)
                .searchCount(searchCount)
                .labels(labels)
                .marketSegment(marketSegment)
                .build();
    }

    // ================= PRIVATE HELPERS =================

    private OrganizationSearchCounter buildNewCounter(Long orgId) {
        return OrganizationSearchCounter.builder()
                .organizationId(orgId)
                .searchCount(0)
                .build();
    }

    private List<LabelResponse> fetchLabelsSafely(Long orgId) {
        try {
            return labelService.getAllLabelsByOrganizationId(orgId);
        } catch (Exception ex) {
            log.error("[LABEL_FETCH_FAILED] orgId={} error={}", orgId, ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    private List<String> fetchMarketSegmentSafely(Long orgId, String website) {

        if (website == null || website.isBlank()) {
            log.warn("[MARKET_SEGMENT_SKIPPED] orgId={} reason=missing_website", orgId);
            return Collections.emptyList();
        }

        try {
            return marketSegmentService.getMarketSegment(website);
        } catch (Exception ex) {
            log.error("[MARKET_SEGMENT_FAILED] orgId={} error={}", orgId, ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    public String updateCoverImage(UpdateCoverImageRequest request) {

        Long orgId = Util.getOrgIdFromToken();
        log.info("[UPDATE COVER IMAGE] orgId={} url={}", orgId, request.getCoverImageUrl());

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH27, orgId));

        organization.setCoverImageUrl(request.getCoverImageUrl());

        organizationRepository.save(organization);

        return "Cover image updated successfully";
    }

    public String updateOrganizationProfile(UpdateOrganizationProfileRequest request) {

        Long orgId = Util.getOrgIdFromToken();
        log.info("[UPDATE ORG PROFILE] orgId={} request={}", orgId, request);

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH27, orgId));

        organization.setName(request.getCompanyName());
        organization.setTagLine(request.getTagline());
        organization.setHeadquarter(request.getHeadquarter());
        organization.setAbout(request.getAbout());

        organization.setFoundedIn(request.getFoundedIn());
        organization.setIndustries(request.getIndustries());
        organization.setServedCustomers(request.getServedCustomers());

        organizationRepository.save(organization);

        return "Organization profile updated successfully";
    }

    public OrganizationProfileSectionResponse getOrganizationProfile() {

        Long orgId = Util.getOrgIdFromToken();
        log.info("[GET ORG PROFILE] orgId={}", orgId);

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH27, orgId));

        return OrganizationProfileSectionResponse.builder()
                .companyName(organization.getName())
                .coverImageURL(organization.getCoverImageUrl())
                .tagline(organization.getTagLine())
                .headquarter(organization.getHeadquarter())
                .about(organization.getAbout())
                .foundedIn(organization.getFoundedIn())
                .industries(organization.getIndustries())
                .servedCustomers(organization.getServedCustomers())
                .build();
    }

    public OrganizationProfileSectionResponse getOrganizationProfileByOrgId(Long orgId) {
        log.info("[GET ORG PROFILE] orgId={}", orgId);

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH27, orgId));

        return OrganizationProfileSectionResponse.builder()
                .companyName(organization.getName())
                .tagline(organization.getTagLine())
                .headquarter(organization.getHeadquarter())
                .about(organization.getAbout())
                .foundedIn(organization.getFoundedIn())
                .industries(organization.getIndustries())
                .servedCustomers(organization.getServedCustomers())
                .build();
    }

    public boolean hasAnyCrmIntegrationActive(Long orgId) {

        List<IntegrationType> crmTypes = List.of(
                IntegrationType.HUBSPOT,
                IntegrationType.ZOHO,
                IntegrationType.SALESFORCE,
                IntegrationType.PIPEDRIVE
        );

        List<IntegrationDetails> integrations =
                integrationRepository.findByOrganizationIdAndIntegrationTypeIn(orgId, crmTypes);

        return integrations.stream()
                .anyMatch(i ->
                        Boolean.TRUE.equals(i.isConnected()) &&
                                i.getRefreshToken() != null &&
                                !i.getRefreshToken().isBlank()
                );
    }

    public ProfileCompletionStatusResponse getProfileCompletionStatus(Long orgId) {

        log.info("[GET_PROFILE_COMPLETION_STATUS] orgId={}", orgId);

        List<OrganizationProfileCompletion> list =
                organizationProfileCompletionRepository.findByOrganizationId(orgId);

        boolean partnerProgram = false;
        boolean dataSource = false;
        boolean profile = false;

        int percentage = 0;

        for (OrganizationProfileCompletion item : list) {

            if (Boolean.TRUE.equals(item.isCompleted())) {

                percentage += item.getWeight();

                switch (item.getType()) {
                    case PARTNER_PROGRAM_PUBLISHED -> partnerProgram = true;
                    case DATA_SOURCE_CONNECTED -> dataSource = true;
                    case PROFILE_COMPLETED -> profile = true;
                }
            }
        }

        return ProfileCompletionStatusResponse.builder()
                .partnerProgramPublished(partnerProgram)
                .dataSourceConnected(dataSource)
                .profileCompleted(profile)
                .completionPercentage(percentage)
                .build();
    }

    @Transactional
    public void markProfileStepCompleted(Long orgId, ProfileCompletionType type) {

        log.info("[MARK_PROFILE_STEP_COMPLETED] orgId={} type={}", orgId, type);

        OrganizationProfileCompletion entity =
                organizationProfileCompletionRepository
                        .findByOrganizationIdAndType(orgId, type)
                        .orElseGet(() -> OrganizationProfileCompletion.builder()
                                .organizationId(orgId)
                                .type(type)
                                .weight(type.getWeight())
                                .completed(false)
                                .build()
                        );

        // If already completed → skip update (idempotent)
        if (Boolean.TRUE.equals(entity.isCompleted())) {
            log.info("[ALREADY_COMPLETED] orgId={} type={}", orgId, type);
            return;
        }

        entity.setCompleted(true);

        organizationProfileCompletionRepository.save(entity);

        log.info("[PROFILE_STEP_COMPLETED] orgId={} type={} weight={}",
                orgId, type, entity.getWeight());
    }

    @Transactional
    public void removeProfileStep(Long orgId, ProfileCompletionType type) {

        log.info("[REMOVE_PROFILE_STEP] orgId={} type={}", orgId, type);

        OrganizationProfileCompletion entity =
                organizationProfileCompletionRepository
                        .findByOrganizationIdAndType(orgId, type)
                        .orElseThrow(() -> new ServiceException(
                                ErrorMessages.SH75, "Profile step not found for orgId: " + orgId
                        ));

        // Option 1: Hard delete
        organizationProfileCompletionRepository.delete(entity);

        log.info("[PROFILE_STEP_REMOVED] orgId={} type={}", orgId, type);
    }

    public CounterStatsResponse getOrgCounterStatsForOrgId(Long orgId) {
        List<CounterSave> orgCounters = counterSaveRepository.findAllByOrgId(orgId);

        int totalClicks = orgCounters.stream()
                .mapToInt(c -> Optional.ofNullable(c.getCounterOnClick()).orElse(0))
                .sum();

        int totalSubmits = orgCounters.stream()
                .mapToInt(c -> Optional.ofNullable(c.getCounterOnSubmit()).orElse(0))
                .sum();

        return new CounterStatsResponse(totalClicks, totalSubmits);
    }

    public int getRandomRating() {
        return ThreadLocalRandom.current().nextInt(1, 6);
    }

    public PartnerPortalBrandingResponse getBrandingByOrgIdForProfileSection(Long orgId) {

        return partnerPortalBrandingRepository.findByOrganizationId(orgId)
                .map(branding -> PartnerPortalBrandingResponse.builder()
                        .id(branding.getId())
                        .title(branding.getTitle())
                        .url(branding.getUrl())
                        .description(branding.getDescription())
                        .timeToFill(getRandomRating())
                        .numberOfQuestions(5)
                        .totalClicks(getOrgCounterStatsForOrgId(orgId).getTotalClicks())
                        .totalSubmits(getOrgCounterStatsForOrgId(orgId).getTotalSubmits())
                        .enabledReferralProgram(branding.getEnabledReferralProgram())
                        .organizationId(branding.getOrganizationId())
                        .PartnerTierAllotted(
                                partnerTierRepository.existsByOrgId(branding.getOrganizationId())
                        )
                        .discountAllotted(
                                partnerTierRepository.existsByOrgId(branding.getOrganizationId())
                        )
                        .createdDate(branding.getCreationTimestamp())
                        .applicationReviewTimeAllotted(true)
                        .build()
                )
                .orElse(null); // or throw exception if required
    }

    public PartnerRankingResponse getTopPartner(Long orgId) {
        List<PartnerRankingResponse> list = organizationCollaborationRepository.getPartnerRanking(orgId);
        return list.isEmpty() ? null : list.get(0);
    }

    public OrganizationStatsResponse getOrganizationStats(Long orgId) {

        log.info("[GET_ORGANIZATION_STATS] orgId={}", orgId);

        int inquiries = 0;
        int views = 0;
        int completionPercentage = 0;
        boolean isEliteBadgeApplicable = false;
        int rank = 0;

        // Null check for orgId
        if (orgId == null) {
            log.warn("Organization ID is null");
            return OrganizationStatsResponse.builder()
                    .views(0)
                    .inquiries(0)
                    .rank(0)
                    .visibilityScore(0.0)
                    .isEliteBadgeApplicable(false)
                    .build();
        }

        try {
            // Seniority Counter
            var optCounter = seniorityCounterRepository.findByOrganizationId(orgId);
            if (optCounter.isPresent() && optCounter.get() != null) {
                inquiries = Optional.ofNullable(optCounter.get().getCounter()).orElse(0);
            }

            // Partner Program Submission
            var partnerProgramSubmission = getOrgCounterStatsForOrgId(orgId);
            if (partnerProgramSubmission != null) {
                inquiries += Optional.ofNullable(partnerProgramSubmission.getTotalSubmits()).orElse(0);
            }

            // Views Counter
            var optViewsCounter = searchCounterRepository.findByOrganizationId(orgId);
            if (optViewsCounter.isPresent() && optViewsCounter.get() != null) {
                views = Optional.ofNullable(optViewsCounter.get().getSearchCount()).orElse(0);
            }

            // Profile Completion
            var profileStatus = getProfileCompletionStatus(orgId);
            if (profileStatus != null) {
                completionPercentage = Optional.ofNullable(profileStatus.getCompletionPercentage()).orElse(0);
            }

            // Elite Badge Logic
            isEliteBadgeApplicable = completionPercentage >= 30;

            // Rank Calculation
            var topPartner = getTopPartner(orgId);
            if (topPartner != null && topPartner.getTotalCount() != null) {
                rank = Math.toIntExact(topPartner.getTotalCount());
            }

        } catch (Exception e) {
            log.error("Error while fetching organization stats for orgId={}", orgId, e);
        }

        // Final Safe Response
        return OrganizationStatsResponse.builder()
                .views(views)
                .inquiries(inquiries)
                .rank(rank)
                .visibilityScore((double) completionPercentage)
                .isEliteBadgeApplicable(isEliteBadgeApplicable)
                .build();
    }

    @Transactional
    public Integer incrementSeniorityCounter() {

        Long orgId = Util.getOrgIdFromToken();

        log.info("[INCREMENT_SENIORITY_COUNTER] orgId={}", orgId);

        OrganizationSeniorityCounter entity =
                seniorityCounterRepository
                        .findByOrganizationId(orgId)
                        .orElseGet(() -> OrganizationSeniorityCounter.builder()
                                .organizationId(orgId)
                                .counter(0)
                                .build()
                        );

        entity.setCounter(entity.getCounter() + 1);

        seniorityCounterRepository.save(entity);

        log.info("[SENIORITY_COUNTER_UPDATED] orgId={} count={}",
                orgId, entity.getCounter());

        return entity.getCounter();
    }

    public String getResponderUrlByOrgId(Long orgId) {

        log.info("[GET_RESPONDER_URL] orgId={}", orgId);

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH27, orgId));

        return organization.getResponderUrl();
    }


}
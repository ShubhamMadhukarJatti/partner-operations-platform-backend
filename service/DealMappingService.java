package com.sharkdom.partnerattribution.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.fasterxml.jackson.databind.JsonNode;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.entity.ai.OverlapRecordEntity;
import com.sharkdom.entity.ai.OverlapRecordFieldEntity;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.model.ai.RecordType;
import com.sharkdom.partnerattribution.dto.*;
import com.sharkdom.partnerattribution.enums.*;
import com.sharkdom.partnerattribution.hubspot.HubspotDataMappingService;
import com.sharkdom.repository.ai.OverlapRecordsRepository;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.salesforce.dto.SalesforceTokenResponse;
import com.sharkdom.salesforce.service.SalesforceAuthService;
import com.sharkdom.util.SharkdomPaginatedResponse;
import com.sharkdom.util.Util;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sharkdom.constants.organization.IntegrationType.SALESFORCE;
import static com.sharkdom.constants.organization.IntegrationType.ZOHO;

@Slf4j
@Service
@RequiredArgsConstructor
public class DealMappingService {

    private final OverlapRecordsRepository overlapRecordsRepository;
    private final HubspotDataMappingService hubspotDataMappingService;
    private final OpportunityScoreService opportunityScoreService;
    private final RestTemplate restTemplate;
    private final ZohoDataMappingService zohoDataMappingService;
    private final IntegrationRepository integrationRepository;
    private final SalesforceAuthService salesforceAuthService;
    private final SalesforceDataMappingService salesforceDataMappingService;
    @Autowired
    @Qualifier("sharedAccountsExecutor")
    private ExecutorService sharedAccountsExecutor;
    @Autowired
    @Qualifier("salesforceTokenCache")
    private Cache<String, SalesforceTokenResponse> salesforceTokenCache;
    @Autowired
    @Qualifier("zohoAccessTokenCache")
    private Cache<String, String> zohoAccessTokenCache;
    /**
     * Maps source and target deals based on website + business stage logic.
     */
    public List<OpportunityDealResponse> mapDealToDealByWebsite(
            List<OpportunityDealResponse> sourceDeals,
            List<OpportunityDealResponse> targetDeals,
            Long sourceOrgId,
            Long targetOrgId
    ) {

        log.info(
                "Started deal-to-deal mapping based on company website using dealId"
        );

        if (sourceDeals == null || sourceDeals.isEmpty()) {
            log.warn("Source deals are null or empty");
            return Collections.emptyList();
        }

        if (targetDeals == null || targetDeals.isEmpty()) {
            log.warn("Target deals are null or empty");
            return Collections.emptyList();
        }

        Map<String, OpportunityDealResponse> targetDealMap =
                new HashMap<>();

        for (OpportunityDealResponse targetDeal : targetDeals) {

            if (targetDeal == null || targetDeal.getDealId() == null) {
                log.warn(
                        "Skipping target deal because dealId is null"
                );
                continue;
            }

            try {

                String website =
                        hubspotDataMappingService
                                .getCompanyWebsiteByDealId(
                                        targetOrgId,
                                        targetDeal.getDealId()
                                );

                if (!isValidWebsite(website)) {
                    log.warn(
                            "Invalid website found for target dealId: {}",
                            targetDeal.getDealId()
                    );
                    continue;
                }

                /**
                 * Set website in target deal
                 */
                targetDeal.setWebsite(website);

                /**
                 * Set target ownerId if needed
                 */
                targetDeal.setTargetOwnerId(
                        targetDeal.getDealOwner()
                );


                targetDealMap.putIfAbsent(
                        normalizeWebsite(website),
                        targetDeal
                );


            } catch (Exception e) {

                log.error(
                        "Failed to fetch website for target dealId: {}",
                        targetDeal.getDealId(),
                        e
                );
            }
        }

        List<OpportunityDealResponse> matchedDeals =
                new ArrayList<>();

        for (OpportunityDealResponse sourceDeal : sourceDeals) {

            if (sourceDeal == null || sourceDeal.getDealId() == null) {
                log.warn(
                        "Skipping source deal because dealId is null"
                );
                continue;
            }

            try {

                String sourceWebsite =
                        hubspotDataMappingService
                                .getCompanyWebsiteByDealId(
                                        sourceOrgId,
                                        sourceDeal.getDealId()
                                );

                if (!isValidWebsite(sourceWebsite)) {
                    log.warn(
                            "Invalid website found for source dealId: {}",
                            sourceDeal.getDealId()
                    );
                    continue;
                }

                String normalizedWebsite =
                        normalizeWebsite(
                                sourceWebsite
                        );

                OpportunityDealResponse targetDeal =
                        targetDealMap.get(
                                normalizedWebsite
                        );

                if (targetDeal == null) {
                    continue;
                }

                DealActionType actionType =
                        resolveDealAction(
                                sourceDeal,
                                targetDeal
                        );

                /**
                 * Source deal data
                 */
                sourceDeal.setWebsite(
                        sourceWebsite
                );

                sourceDeal.setDealActionType(
                        actionType
                );

                /**
                 * Mapping matched deal info
                 */
                sourceDeal.setTargetPartnerDealId(
                        targetDeal.getDealId()
                );

                /**
                 * Source owner
                 */
                sourceDeal.setOwnerId(
                        sourceDeal.getDealOwner()
                );

                /**
                 * Target owner
                 */
                sourceDeal.setTargetOwnerId(
                        targetDeal.getDealOwner()
                );

                matchedDeals.add(
                        sourceDeal
                );

                log.info(
                        "Deal matched successfully. sourceDealId={}, targetDealId={}, website={}",
                        sourceDeal.getDealId(),
                        targetDeal.getDealId(),
                        sourceWebsite
                );

            } catch (Exception e) {

                log.error(
                        "Failed to fetch website for source dealId: {}",
                        sourceDeal.getDealId(),
                        e
                );
            }
        }

        log.info(
                "Deal-to-deal mapping completed. sourceCount={}, targetCount={}, matchedCount={}",
                sourceDeals.size(),
                targetDeals.size(),
                matchedDeals.size()
        );

        return matchedDeals;
    }

    /**
     * Resolve business action based on source and target deal stages.
     */
    private DealActionType resolveDealAction(
            OpportunityDealResponse sourceDeal,
            OpportunityDealResponse targetDeal
    ) {

        StageType source = mapStage(sourceDeal);
        StageType target = mapStage(targetDeal);

        /**
         * Rule 1:
         * Active/Closed + Active/Closed = START_CO_SELL
         */
        if (isActiveOrClosed(source) && isActiveOrClosed(target)) {
            return DealActionType.START_CO_SELL;
        }

        /**
         * Rule 2:
         * Ad Pipeline + Closed Customer = REQUEST_INTRO
         */
        if (source == StageType.AD_PIPELINE
                && target == StageType.CLOSED_CUSTOMER) {
            return DealActionType.REQUEST_INTRO;
        }

        /**
         * Rule 3:
         * Ad Pipeline + Active Pipeline = ADD_TO_PIPELINE
         */
        if (source == StageType.AD_PIPELINE
                && target == StageType.ACTIVE_PIPELINE) {
            return DealActionType.ADD_TO_PIPELINE;
        }

        return null;
    }

    /**
     * Check if stage is active or closed.
     */
    private boolean isActiveOrClosed(StageType stage) {
        return stage == StageType.ACTIVE_PIPELINE
                || stage == StageType.CLOSED_CUSTOMER;
    }

    /**
     * Fetch overlap records for opportunities.
     */
    public List<OpportunityDealResponse> getOverlapRecords(Long organizationId) {

        log.info("Fetching overlap records for organizationId={}", organizationId);

        if (organizationId == null) {
            log.error("OrganizationId is null");
            throw new ServiceException(ErrorMessages.SH199);
        }

        try {
            List<OverlapRecordEntity> overlapRecords =
                    overlapRecordsRepository.findOverlapRecords(
                            organizationId,
                            RecordType.OPPORTUNITY
                    );

            if (overlapRecords == null || overlapRecords.isEmpty()) {
                log.info(
                        "No overlap records found for organizationId={}",
                        organizationId
                );
                return Collections.emptyList();
            }

            List<OpportunityDealResponse> responses =
                    overlapRecords.stream()
                            .filter(Objects::nonNull)
                            .map(OverlapRecordEntity::getFields)
                            .filter(Objects::nonNull)
                            .flatMap(Collection::stream)
                            .filter(Objects::nonNull)
                            .map(this::buildOpportunityDealResponse)
                            .toList();

            log.info(
                    "Successfully fetched overlap records for organizationId={}, totalRecords={}",
                    organizationId,
                    responses.size()
            );

            return responses;

        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error(
                    "Error while fetching overlap records for organizationId={}",
                    organizationId,
                    ex
            );
            throw new ServiceException(
                    ErrorMessages.SH200,
                    organizationId
            );
        }
    }

    /**
     * Converts entity into response DTO.
     */
    private OpportunityDealResponse buildOpportunityDealResponse(
            OverlapRecordFieldEntity field
    ) {
        return OpportunityDealResponse.builder()
                .dealName(field.getDealName())
                .website(field.getWebsite())
                .dealStage(field.getDealStage())
                .associatedCompanyId(field.getAssociatedCompanyId())
                .dealOwner(field.getDealOwner())
                .amountAcv(field.getAmountAcv())
                .closeDate(field.getCloseDate())
                .dealId(field.getDealId())
                .pipeline(field.getPipeline())
                .lastActivityDate(field.getLastActivityDate())
                .dealType(field.getDealType())
                .associatedContactId(field.getAssociatedContactId())
                .build();
    }

    /**
     * Validates website field.
     */
    private boolean isValidWebsite(String website) {
        return website != null && !website.trim().isBlank();
    }

    /**
     * Normalize website for accurate comparison.
     */
    private String normalizeWebsite(String website) {

        if (website == null) {
            return "";
        }

        return website.trim()
                .toLowerCase()
                .replaceFirst("^https?://", "")
                .replaceFirst("^www\\.", "")
                .replaceAll("/$", "");
    }

    /**
     * Fetch deals of two organizations and find overlap.
     */
    public List<OpportunityDealResponse> getDealOverlapBetweenOrganizations(
            Long sourceOrganizationId,
            Long targetOrganizationId
    ) {

        log.info(
                "Fetching deal overlap between sourceOrganizationId={} and targetOrganizationId={}",
                sourceOrganizationId,
                targetOrganizationId
        );

        if (sourceOrganizationId == null) {
            log.error("Source organizationId is null");
            throw new ServiceException(ErrorMessages.SH199);
        }

        if (targetOrganizationId == null) {
            log.error("Target organizationId is null");
            throw new ServiceException(ErrorMessages.SH199);
        }

        List<OpportunityDealResponse> sourceDeals =
                getOverlapRecords(sourceOrganizationId);

        List<OpportunityDealResponse> targetDeals =
                getOverlapRecords(targetOrganizationId);

        if (sourceDeals.isEmpty() || targetDeals.isEmpty()) {
            log.info(
                    "No overlap possible. sourceDealsCount={}, targetDealsCount={}",
                    sourceDeals.size(),
                    targetDeals.size()
            );
            return Collections.emptyList();
        }

        List<OpportunityDealResponse> overlappedDeals =
                mapDealToDealByWebsite(sourceDeals, targetDeals, sourceOrganizationId, targetOrganizationId);

        log.info(
                "Overlap fetched successfully between organizations. overlapCount={}",
                overlappedDeals.size()
        );

        return overlappedDeals;
    }

    /**
     * Internal stage bucket enum.
     */
    private enum StageType {
        AD_PIPELINE,
        ACTIVE_PIPELINE,
        CLOSED_CUSTOMER
    }

    /**
     * Converts deal into stage bucket.
     *
     * Business rules:
     * 1. closeDate exists → CLOSED_CUSTOMER
     * 2. dealStage null/empty → AD_PIPELINE
     * 3. any dealStage value → ACTIVE_PIPELINE
     */
    private StageType mapStage(OpportunityDealResponse deal) {

        if (deal == null) {
            return StageType.AD_PIPELINE;
        }

        /**
         * Highest priority:
         * If close date exists,
         * treat as closed customer
         */
        if (deal.getCloseDate() != null
                && !deal.getCloseDate().toString().trim().isBlank()) {
            return StageType.CLOSED_CUSTOMER;
        }

        /**
         * No stage means ad pipeline
         */
        if (deal.getDealStage() == null
                || deal.getDealStage().isBlank()) {
            return StageType.AD_PIPELINE;
        }

        /**
         * Any stage means active pipeline
         */
        return StageType.ACTIVE_PIPELINE;
    }


    private SharedAccountResponse buildSharedAccountResponse(
            OpportunityDealResponse sourceDeal,
            OpportunityDealResponse targetDeal,
            Integer opportunityScore,
            String estimatedAcv
    ) {

        PartnerAttributionAction action =
                mapPartnerAction(sourceDeal.getDealActionType());

        return SharedAccountResponse.builder()
                .accountName(sourceDeal.getDealName())
                .website(sourceDeal.getWebsite())
                .dealId(sourceDeal.getDealId())
                .partnerDealId(targetDeal.getDealId())
                .ownerId(sourceDeal.getOwnerId())
                .partnerDealOwnerId(targetDeal.getTargetOwnerId())
                .yourStage(mapStage(sourceDeal).name())
                .partnerStage(mapStage(targetDeal).name())
                .opportunityScore(opportunityScore)
                .estimatedAcv(estimatedAcv)
                .action(action)
                .motion(resolveCoSellMotion(action))
                .overlapType(resolveOverlapType(action))
                .build();
    }

    private PartnerAttributionAction mapPartnerAction(
            DealActionType actionType
    ) {

        if (actionType == null) {
            return PartnerAttributionAction.NO_ACTION;
        }

        return switch (actionType) {
            case ADD_TO_PIPELINE ->
                    PartnerAttributionAction.ADD_TO_PIPELINE;

            case REQUEST_INTRO ->
                    PartnerAttributionAction.REQUEST_INTRO;

            case START_CO_SELL ->
                    PartnerAttributionAction.START_CO_SELL;
        };
    }

    private OverlapType resolveOverlapType(
            PartnerAttributionAction action
    ) {

        return switch (action) {

            case START_CO_SELL ->
                    OverlapType.CO_SELL_READY;

            case REQUEST_INTRO ->
                    OverlapType.HOT_OVERLAP;

            case ADD_TO_PIPELINE ->
                    OverlapType.MONITOR;

            default ->
                    OverlapType.LOW_PRIORITY;
        };
    }

    private CoSellMotion resolveCoSellMotion(
            PartnerAttributionAction action
    ) {

        return switch (action) {

            case START_CO_SELL ->
                    CoSellMotion.JOINT_MOTION;

            case REQUEST_INTRO ->
                    CoSellMotion.WARM_DOOR_ACCESS;

            case ADD_TO_PIPELINE ->
                    CoSellMotion.ESCALATE_MQL;

            case MONITOR ->
                    CoSellMotion.LOW_PRIORITY_ENGAGEMENT;

            default ->
                    CoSellMotion.PARTNER_VALIDATION;
        };
    }

    @Transactional
    public boolean isNumberOfEmployeesMatched(
            Long sourceOrganizationId,
            String sourceDealId,
            Long targetOrganizationId,
            String targetDealId
    ) {

        log.info(
                "Comparing number of employees for sourceDealId: {} and targetDealId: {}",
                sourceDealId,
                targetDealId
        );

        String sourceNumberOfEmployees =
                hubspotDataMappingService.getCompanyNumberOfEmployeesByDealId(
                        sourceOrganizationId,
                        sourceDealId
                );

        String targetNumberOfEmployees =
                hubspotDataMappingService.getCompanyNumberOfEmployeesByDealId(
                        targetOrganizationId,
                        targetDealId
                );

        if (sourceNumberOfEmployees == null) {

            log.warn(
                    "Source company number of employees not found for dealId: {}",
                    sourceDealId
            );

            return false;
        }

        if (targetNumberOfEmployees == null) {

            log.warn(
                    "Target company number of employees not found for dealId: {}",
                    targetDealId
            );

            return false;
        }

        boolean isMatched =
                sourceNumberOfEmployees
                        .trim()
                        .equalsIgnoreCase(
                                targetNumberOfEmployees.trim()
                        );

        log.info(
                "Number of employees comparison completed. sourceValue={}, targetValue={}, matched={}",
                sourceNumberOfEmployees,
                targetNumberOfEmployees,
                isMatched
        );

        return isMatched;
    }

    @Transactional
    public boolean isIndustryMatched(
            Long sourceOrganizationId,
            String sourceDealId,
            Long targetOrganizationId,
            String targetDealId
    ) {

        log.info(
                "Comparing industry for sourceDealId: {} and targetDealId: {}",
                sourceDealId,
                targetDealId
        );

        String sourceIndustry =
                hubspotDataMappingService.getCompanyIndustryByDealId(
                        sourceOrganizationId,
                        sourceDealId
                );

        String targetIndustry =
                hubspotDataMappingService.getCompanyIndustryByDealId(
                        targetOrganizationId,
                        targetDealId
                );

        if (sourceIndustry == null) {

            log.warn(
                    "Source company industry not found for dealId: {}",
                    sourceDealId
            );

            return false;
        }

        if (targetIndustry == null) {

            log.warn(
                    "Target company industry not found for dealId: {}",
                    targetDealId
            );

            return false;
        }

        boolean isMatched =
                sourceIndustry
                        .trim()
                        .equalsIgnoreCase(
                                targetIndustry.trim()
                        );

        log.info(
                "Industry comparison completed. sourceValue={}, targetValue={}, matched={}",
                sourceIndustry,
                targetIndustry,
                isMatched
        );

        return isMatched;
    }

    @Transactional
    public boolean isCountryMatched(
            Long sourceOrganizationId,
            String sourceDealId,
            Long targetOrganizationId,
            String targetDealId
    ) {

        log.info(
                "Comparing country for sourceDealId: {} and targetDealId: {}",
                sourceDealId,
                targetDealId
        );

        String sourceCountry =
                hubspotDataMappingService.getCompanyCountryByDealId(
                        sourceOrganizationId,
                        sourceDealId
                );

        String targetCountry =
                hubspotDataMappingService.getCompanyCountryByDealId(
                        targetOrganizationId,
                        targetDealId
                );

        if (sourceCountry == null) {

            log.warn(
                    "Source company country not found for dealId: {}",
                    sourceDealId
            );

            return false;
        }

        if (targetCountry == null) {

            log.warn(
                    "Target company country not found for dealId: {}",
                    targetDealId
            );

            return false;
        }

        boolean isMatched =
                sourceCountry
                        .trim()
                        .equalsIgnoreCase(
                                targetCountry.trim()
                        );

        log.info(
                "Country comparison completed. sourceValue={}, targetValue={}, matched={}",
                sourceCountry,
                targetCountry,
                isMatched
        );

        return isMatched;
    }


    public SharkdomPaginatedResponse<SharedAccountDTO> getSharedAccounts(
            Long targetOrganizationId
    ) {
        return getSharedAccounts(
                targetOrganizationId,
                1,
                10,
                "score",
                "all",
                null
        );
    }

    public SharkdomPaginatedResponse<SharedAccountDTO> getSharedAccounts(
            Long targetOrganizationId,
            int page,
            int size,
            String sort,
            String filter,
            String search
    ) {
        log.info(
                "Fetching shared accounts for targetOrganizationId={}",
                targetOrganizationId
        );

        Long sourceOrganizationId = Util.getOrgIdFromToken();
//    Long sourceOrganizationId = 1579L;

        if (sourceOrganizationId == null) {
            log.error("Source organization id not found from token");
            throw new ServiceException(ErrorMessages.SH199);
        }

        List<OpportunityDealResponse> sourceDeals =
                getOverlapRecords(sourceOrganizationId);

        List<OpportunityDealResponse> targetDeals =
                getOverlapRecords(targetOrganizationId);

        if (sourceDeals.isEmpty() || targetDeals.isEmpty()) {

            log.info(
                    "No shared accounts found. sourceDeals={}, targetDeals={}",
                    sourceDeals.size(),
                    targetDeals.size()
            );

            return SharkdomPaginatedResponse.<SharedAccountDTO>builder()
                    .content(Collections.emptyList())
                    .size(0)
                    .build();
        }

        Set<IntegrationType> sharedConnectedIntegrations =
                getSharedConnectedCrmIntegrations(
                        sourceOrganizationId,
                        targetOrganizationId
                );

        CompletableFuture<List<SharedAccountDTO>> hubspotFuture =
                sharedConnectedIntegrations.contains(IntegrationType.HUBSPOT)
                        ? CompletableFuture.supplyAsync(() -> {

                    List<SharedAccountDTO> responses = new ArrayList<>();

                    List<OpportunityDealResponse> overlappedDealsHubSpot =
                            mapDealToDealByWebsite(
                                    sourceDeals,
                                    targetDeals,
                                    sourceOrganizationId,
                                    targetOrganizationId
                            );

                    Map<String, OpportunityDealResponse> targetDealMap =
                            overlappedDealsHubSpot.stream()
                                    .filter(Objects::nonNull)
                                    .filter(deal -> deal.getWebsite() != null)
                                    .collect(
                                            Collectors.toMap(
                                                    deal -> normalizeWebsite(deal.getWebsite()),
                                                    deal -> deal,
                                                    (existing, replacement) -> existing
                                            )
                                    );

                    for (OpportunityDealResponse sourceDeal : overlappedDealsHubSpot) {

                        OpportunityDealResponse targetDeal =
                                targetDealMap.get(
                                        normalizeWebsite(sourceDeal.getWebsite())
                                );

                        if (targetDeal == null) {
                            continue;
                        }

                        boolean companySizeMatch =
                                isNumberOfEmployeesMatched(
                                        sourceOrganizationId,
                                        sourceDeal.getDealId(),
                                        targetOrganizationId,
                                        targetDeal.getDealId()
                                );

                        boolean industryMatch =
                                isIndustryMatched(
                                        sourceOrganizationId,
                                        sourceDeal.getDealId(),
                                        targetOrganizationId,
                                        targetDeal.getDealId()
                                );

                        boolean geographyMatch =
                                isCountryMatched(
                                        sourceOrganizationId,
                                        sourceDeal.getDealId(),
                                        targetOrganizationId,
                                        targetDeal.getDealId()
                                );

                        DealStageAlignment stageAlignment =
                                resolveStageAlignment(
                                        sourceDeal,
                                        targetDeal
                                );

                        int opportunityScore =
                                opportunityScoreService.calculateScore(
                                        stageAlignment,
                                        EngagementRecency.LESS_THAN_7_DAYS,
                                        companySizeMatch,
                                        industryMatch,
                                        geographyMatch
                                );

                        SharedAccountResponse response =
                                buildSharedAccountResponse(
                                        sourceDeal,
                                        targetDeal,
                                        opportunityScore,
                                        sourceDeal.getAmountAcv()
                                );

                        responses.add(mapToSharedAccountDto(response));
                    }

                    return responses;
                }, sharedAccountsExecutor)
                        : CompletableFuture.completedFuture(Collections.emptyList());

        CompletableFuture<List<SharedAccountDTO>> zohoFuture =
                sharedConnectedIntegrations.contains(ZOHO)
                        ? CompletableFuture.supplyAsync(() -> {

                    List<SharedAccountDTO> responses = new ArrayList<>();

                    List<OpportunityDealResponse> overlappedDealsZoho =
                            mapZohoDeals(
                                    sourceDeals,
                                    targetDeals,
                                    sourceOrganizationId,
                                    targetOrganizationId
                            );

                    Map<String, OpportunityDealResponse> targetDealMap =
                            overlappedDealsZoho.stream()
                                    .filter(Objects::nonNull)
                                    .filter(deal -> deal.getWebsite() != null)
                                    .collect(
                                            Collectors.toMap(
                                                    deal -> normalizeWebsite(deal.getWebsite()),
                                                    deal -> deal,
                                                    (existing, replacement) -> existing
                                            )
                                    );

                    for (OpportunityDealResponse sourceDeal : overlappedDealsZoho) {

                        OpportunityDealResponse targetDeal =
                                targetDealMap.get(
                                        normalizeWebsite(sourceDeal.getWebsite())
                                );

                        if (targetDeal == null) {
                            continue;
                        }

                        ZohoDealData sourceData =
                                getZohoDealData(
                                        sourceOrganizationId,
                                        sourceDeal.getDealId()
                                );

                        ZohoDealData targetData =
                                getZohoDealData(
                                        targetOrganizationId,
                                        targetDeal.getDealId()
                                );

                        boolean companySizeMatch =
                                Objects.equals(
                                        sourceData.getEmployees(),
                                        targetData.getEmployees()
                                );

                        boolean industryMatch =
                                StringUtils.equalsIgnoreCase(
                                        sourceData.getIndustry(),
                                        targetData.getIndustry()
                                );

                        boolean geographyMatch =
                                StringUtils.equalsIgnoreCase(
                                        sourceData.getCountry(),
                                        targetData.getCountry()
                                );

                        DealStageAlignment stageAlignment =
                                resolveStageAlignment(
                                        sourceDeal,
                                        targetDeal
                                );

                        int opportunityScore =
                                opportunityScoreService.calculateScore(
                                        stageAlignment,
                                        EngagementRecency.LESS_THAN_7_DAYS,
                                        companySizeMatch,
                                        industryMatch,
                                        geographyMatch
                                );

                        SharedAccountResponse response =
                                buildSharedAccountResponse(
                                        sourceDeal,
                                        targetDeal,
                                        opportunityScore,
                                        sourceDeal.getAmountAcv()
                                );

                        responses.add(mapToSharedAccountDto(response));
                    }

                    return responses;
                }, sharedAccountsExecutor)
                        : CompletableFuture.completedFuture(Collections.emptyList());

        CompletableFuture<List<SharedAccountDTO>> salesforceFuture =
                sharedConnectedIntegrations.contains(SALESFORCE)
                        ? CompletableFuture.supplyAsync(() -> {
                    List<OpportunityDealResponse> overlappedDealsSalesforce =
                            mapSalesforceDeals(
                                    sourceDeals,
                                    targetDeals,
                                    sourceOrganizationId,
                                    targetOrganizationId
                            );

                    Map<String, OpportunityDealResponse> targetDealMap =
                            targetDeals.stream()
                                    .filter(Objects::nonNull)
                                    .filter(deal -> deal.getDealId() != null)
                                    .collect(
                                            Collectors.toMap(
                                                    OpportunityDealResponse::getDealId,
                                                    deal -> deal,
                                                    (existing, replacement) -> existing
                                            )
                                    );

                    return mapInParallel(
                            overlappedDealsSalesforce,
                            sourceDeal -> {

                        OpportunityDealResponse targetDeal =
                                targetDealMap.get(
                                        sourceDeal.getTargetPartnerDealId()
                                );

                        if (targetDeal == null) {
                            return null;
                        }

                        SalesforceDealData sourceData =
                                getSalesforceDealData(
                                        sourceOrganizationId,
                                        sourceDeal.getDealId()
                                );

                        SalesforceDealData targetData =
                                getSalesforceDealData(
                                        targetOrganizationId,
                                        targetDeal.getDealId()
                                );

                        if (sourceData == null || targetData == null) {
                            return null;
                        }

                        boolean companySizeMatch =
                                Objects.equals(
                                        sourceData.getEmployees(),
                                        targetData.getEmployees()
                                );

                        boolean industryMatch =
                                StringUtils.equalsIgnoreCase(
                                        sourceData.getIndustry(),
                                        targetData.getIndustry()
                                );

                        boolean geographyMatch =
                                StringUtils.equalsIgnoreCase(
                                        sourceData.getCountry(),
                                        targetData.getCountry()
                                );

                        DealStageAlignment stageAlignment =
                                resolveStageAlignment(
                                        sourceDeal,
                                        targetDeal
                                );

                        int opportunityScore =
                                opportunityScoreService.calculateScore(
                                        stageAlignment,
                                        EngagementRecency.LESS_THAN_7_DAYS,
                                        companySizeMatch,
                                        industryMatch,
                                        geographyMatch
                                );

                        SharedAccountResponse response =
                                buildSharedAccountResponse(
                                        sourceDeal,
                                        targetDeal,
                                        opportunityScore,
                                        sourceDeal.getAmountAcv()
                                );

                        return mapToSharedAccountDto(response);
                    });
                }, sharedAccountsExecutor)
                        : CompletableFuture.completedFuture(Collections.emptyList());


        List<SharedAccountDTO> responses =
                CompletableFuture.allOf(hubspotFuture, zohoFuture, salesforceFuture)
                        .thenApply(v -> {
                            List<SharedAccountDTO> result = new ArrayList<>();
                            result.addAll(hubspotFuture.join());
                            result.addAll(zohoFuture.join());
                            result.addAll(salesforceFuture.join());
                            return result;
                        })
                        .join();

        if (responses.isEmpty()) {
            return SharkdomPaginatedResponse.<SharedAccountDTO>builder()
                    .content(Collections.emptyList())
                    .page(Math.max(page, 1))
                    .size(0)
                    .totalElements(0)
                    .totalPages(0)
                    .last(true)
                    .build();
        }

        List<SharedAccountDTO> filteredResponses =
                applySharedAccountQuery(
                        responses,
                        sort,
                        filter,
                        search
                );

        int requestedPage = Math.max(page, 1);
        int requestedSize = Math.max(size, 1);
        int totalElements = filteredResponses.size();
        int totalPages =
                totalElements == 0
                        ? 0
                        : (int) Math.ceil((double) totalElements / requestedSize);
        int fromIndex =
                Math.min((requestedPage - 1) * requestedSize, totalElements);
        int toIndex =
                Math.min(fromIndex + requestedSize, totalElements);
        List<SharedAccountDTO> pageContent =
                filteredResponses.subList(fromIndex, toIndex);

        return SharkdomPaginatedResponse.<SharedAccountDTO>builder()
                .content(pageContent)
                .page(requestedPage)
                .size(pageContent.size())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(requestedPage >= totalPages)
                .build();
    }

    private Set<IntegrationType> getSharedConnectedCrmIntegrations(
            Long sourceOrganizationId,
            Long targetOrganizationId
    ) {
        Set<IntegrationType> sourceIntegrations =
                getConnectedCrmIntegrations(sourceOrganizationId);

        Set<IntegrationType> targetIntegrations =
                getConnectedCrmIntegrations(targetOrganizationId);

        sourceIntegrations.retainAll(targetIntegrations);

        log.info(
                "Shared connected CRM integrations for sourceOrganizationId={} and targetOrganizationId={} are {}",
                sourceOrganizationId,
                targetOrganizationId,
                sourceIntegrations
        );

        return sourceIntegrations;
    }

    private Set<IntegrationType> getConnectedCrmIntegrations(Long organizationId) {
        if (organizationId == null) {
            return EnumSet.noneOf(IntegrationType.class);
        }

        return integrationRepository
                .findAllByOrganizationIdAndRefreshTokenIsNotNull(organizationId)
                .stream()
                .filter(IntegrationDetails::isConnected)
                .map(IntegrationDetails::getIntegrationType)
                .filter(this::isSharedAccountCrmIntegration)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(IntegrationType.class)));
    }

    private boolean isSharedAccountCrmIntegration(IntegrationType integrationType) {
        return integrationType == IntegrationType.HUBSPOT
                || integrationType == ZOHO
                || integrationType == SALESFORCE;
    }

    private List<SharedAccountDTO> applySharedAccountQuery(
            List<SharedAccountDTO> accounts,
            String sort,
            String filter,
            String search
    ) {

        Stream<SharedAccountDTO> stream =
                accounts.stream();

        if (StringUtils.isNotBlank(search)) {
            String normalizedSearch =
                    search.trim().toLowerCase();

            stream =
                    stream.filter(account ->
                            containsIgnoreCase(account.getName(), normalizedSearch)
                                    || containsIgnoreCase(account.getDomain(), normalizedSearch)
                    );
        }

        if (StringUtils.isNotBlank(filter)
                && !"all".equalsIgnoreCase(filter.trim())) {

            String normalizedFilter =
                    filter.trim();

            stream =
                    stream.filter(account ->
                            StringUtils.equalsIgnoreCase(account.getOverlapType(), normalizedFilter)
                                    || StringUtils.equalsIgnoreCase(account.getRecommendedAction(), normalizedFilter)
                    );
        }

        Comparator<SharedAccountDTO> comparator =
                resolveSharedAccountComparator(sort);

        return stream
                .sorted(comparator)
                .toList();
    }

    private boolean containsIgnoreCase(String value, String normalizedSearch) {
        return value != null
                && value.toLowerCase().contains(normalizedSearch);
    }

    private Comparator<SharedAccountDTO> resolveSharedAccountComparator(String sort) {

        if (StringUtils.equalsIgnoreCase(sort, "name")) {
            return Comparator.comparing(
                    SharedAccountDTO::getName,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
            );
        }

        if (StringUtils.equalsIgnoreCase(sort, "acv")) {
            return Comparator.comparing(
                    SharedAccountDTO::getEstimatedACV,
                    Comparator.nullsLast(Comparator.reverseOrder())
            );
        }

        return Comparator.comparing(
                SharedAccountDTO::getOpportunityScore,
                Comparator.nullsLast(Comparator.reverseOrder())
        );
    }

    private ZohoDealData getZohoDealData(
            Long organizationId,
            String dealId
    ) {

        String accessToken = refreshAccessToken(organizationId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity =
                new HttpEntity<>(headers);

        String dealUrl =
                "https://www.zohoapis.in/crm/v7/Deals/" + dealId;

        ResponseEntity<JsonNode> dealResponse =
                restTemplate.exchange(
                        dealUrl,
                        HttpMethod.GET,
                        entity,
                        JsonNode.class
                );

        JsonNode deal =
                dealResponse.getBody()
                        .path("data")
                        .get(0);

        String stage =
                deal.path("Stage").asText(null);

        String accountId =
                deal.path("Account_Name")
                        .path("id")
                        .asText(null);

        if (accountId == null) {
            return null;
        }

        String accountUrl =
                "https://www.zohoapis.in/crm/v7/Accounts/" + accountId;

        ResponseEntity<JsonNode> accountResponse =
                restTemplate.exchange(
                        accountUrl,
                        HttpMethod.GET,
                        entity,
                        JsonNode.class
                );

        JsonNode account =
                accountResponse.getBody()
                        .path("data")
                        .get(0);

        return ZohoDealData.builder()
                .employees(
                        account.path("Employees").isMissingNode()
                                ? null
                                : account.path("Employees").asInt()
                )
                .industry(
                        account.path("Industry").asText(null)
                )
                .country(
                        account.path("Billing_Country").asText(null)
                )
                .stage(stage)
                .build();
    }

    private List<OpportunityDealResponse> mapZohoDeals(
            List<OpportunityDealResponse> sourceDeals,
            List<OpportunityDealResponse> targetDeals,
            Long sourceOrganizationId,
            Long targetOrganizationId) {

        log.info("Started Zoho deal-to-deal mapping based on website");

        if (sourceDeals == null || sourceDeals.isEmpty()) {
            return Collections.emptyList();
        }

        if (targetDeals == null || targetDeals.isEmpty()) {
            return Collections.emptyList();
        }

        String accessTokenTarget = refreshAccessToken(targetOrganizationId);

        Map<String, OpportunityDealResponse> targetDealMap =
                mapInParallel(
                        targetDeals,
                        targetDeal -> {

                            if (targetDeal == null || targetDeal.getDealId() == null) {
                                return null;
                            }

                            try {

                                String website =
                                        zohoDataMappingService.getWebsiteByDealId(
                                                targetOrganizationId,
                                                targetDeal.getDealId(),
                                                accessTokenTarget
                                        );

                                if (!isValidWebsite(website)) {
                                    return null;
                                }

                                targetDeal.setWebsite(website);
                                targetDeal.setTargetOwnerId(targetDeal.getDealOwner());

                                return targetDeal;

                            } catch (Exception e) {

                                log.error(
                                        "Failed to fetch website for target dealId: {}",
                                        targetDeal.getDealId(),
                                        e
                                );
                                return null;
                            }
                        }).stream()
                        .collect(Collectors.toMap(
                                deal -> normalizeWebsite(deal.getWebsite()),
                                Function.identity(),
                                (existing, replacement) -> existing
                        ));

        List<OpportunityDealResponse> matchedDeals = new ArrayList<>();

        String accessTokenSource = refreshAccessToken(sourceOrganizationId);

        for (OpportunityDealResponse sourceDeal : sourceDeals) {

            if (sourceDeal == null || sourceDeal.getDealId() == null) {
                continue;
            }

            try {

                String sourceWebsite =
                        zohoDataMappingService.getWebsiteByDealId(
                                sourceOrganizationId,
                                sourceDeal.getDealId(),
                                accessTokenSource
                        );

                if (!isValidWebsite(sourceWebsite)) {
                    continue;
                }

                OpportunityDealResponse targetDeal =
                        targetDealMap.get(
                                normalizeWebsite(sourceWebsite)
                        );

                if (targetDeal == null) {
                    continue;
                }

                DealActionType actionType =
                        resolveDealAction(
                                sourceDeal,
                                targetDeal
                        );

                sourceDeal.setWebsite(sourceWebsite);
                sourceDeal.setDealActionType(actionType);

                sourceDeal.setTargetPartnerDealId(
                        targetDeal.getDealId()
                );

                sourceDeal.setOwnerId(
                        sourceDeal.getDealOwner()
                );

                sourceDeal.setTargetOwnerId(
                        targetDeal.getDealOwner()
                );

                matchedDeals.add(sourceDeal);

                log.info(
                        "Zoho deal matched. sourceDealId={}, targetDealId={}, website={}",
                        sourceDeal.getDealId(),
                        targetDeal.getDealId(),
                        sourceWebsite
                );

            } catch (Exception e) {

                log.error(
                        "Failed to fetch website for source dealId: {}",
                        sourceDeal.getDealId(),
                        e
                );
            }
        }

        log.info(
                "Zoho deal mapping completed. sourceCount={}, targetCount={}, matchedCount={}",
                sourceDeals.size(),
                targetDeals.size(),
                matchedDeals.size()
        );

        return matchedDeals;
    }

    public String refreshAccessToken(Long orgId) {
        String cacheKey =
                "ZOHO_ACCESS_TOKEN_" + orgId;

        return zohoAccessTokenCache.get(
                cacheKey,
                key -> generateZohoAccessToken(orgId)
        );
    }

    private String generateZohoAccessToken(Long orgId) {

        String refreshToken = integrationRepository
                .findByOrganizationIdAndIntegrationTypeAndIsConnectedAndRefreshTokenIsNotNull(
                        orgId, ZOHO, true)
                .stream()
                .findFirst()
                .map(IntegrationDetails::getRefreshToken)
                .orElse(null);

        String url = UriComponentsBuilder
                .fromHttpUrl("https://accounts.zoho.in/oauth/v2/token")
                .queryParam("refresh_token", refreshToken)
                .queryParam("client_id", "1000.44KGP1HCZ798VI3GYNO1Q93Y29H2RW")
                .queryParam("client_secret", "dbf5571829ca1d7809b4e799e20056d1952800fdba")
                .queryParam("grant_type", "refresh_token")
                .toUriString();

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(new HttpHeaders()),
                    Map.class
            );

            log.info("Zoho token response: {}", response.getBody());

            return (String) response.getBody().get("access_token");

        } catch (HttpStatusCodeException ex) {
            log.error("Zoho token request failed. Status={}, Body={}",
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString());

            throw ex;
        }
    }

    private DealStageAlignment resolveStageAlignment(
            OpportunityDealResponse sourceDeal,
            OpportunityDealResponse targetDeal
    ) {

        StageType sourceStage = mapStage(sourceDeal);
        StageType targetStage = mapStage(targetDeal);

        if (sourceStage == StageType.ACTIVE_PIPELINE
                && targetStage == StageType.ACTIVE_PIPELINE) {
            return DealStageAlignment.BOTH_ACTIVE_PIPELINE;
        }

        if ((sourceStage == StageType.ACTIVE_PIPELINE
                && targetStage == StageType.AD_PIPELINE)
                || (sourceStage == StageType.AD_PIPELINE
                && targetStage == StageType.ACTIVE_PIPELINE)) {
            return DealStageAlignment.ONE_PIPELINE_ONE_PROSPECT;
        }

        if (sourceStage == StageType.CLOSED_CUSTOMER
                || targetStage == StageType.CLOSED_CUSTOMER) {
            return DealStageAlignment.ONE_CLOSED_CUSTOMER;
        }

        return DealStageAlignment.NEITHER_PIPELINE;
    }

    private SharedAccountDTO mapToSharedAccountDto(
            SharedAccountResponse response
    ) {

        SharedAccountDTO dto =
                new SharedAccountDTO();

        dto.setName(
                response.getAccountName()
        );

        dto.setDomain(
                response.getWebsite()
        );

        dto.setOverlapType(
                String.valueOf(response.getOverlapType())
        );

        dto.setOpportunityScore(
                response.getOpportunityScore()
        );

        dto.setYourStage(
                response.getYourStage()
        );

        dto.setPartnerStage(
                response.getPartnerStage()
        );

        if (response.getEstimatedAcv() != null) {

            try {

                dto.setEstimatedACV(
                        Integer.valueOf(
                                response.getEstimatedAcv()
                        )
                );

            } catch (Exception e) {

                log.warn(
                        "Unable to parse estimated ACV: {}",
                        response.getEstimatedAcv()
                );
            }
        }

        dto.setRecommendedAction(
                response.getAction() != null
                        ? response.getAction().name()
                        : null
        );

        dto.setTargetPartnerDealId(
                response.getPartnerDealId()
        );

        dto.setCurrentPartnerDealId(
                response.getDealId()
        );

        dto.setCurrentPartnerDealOwnerId(
                response.getOwnerId()
        );

        dto.setTargetPartnerDealOwnerId(
                response.getPartnerDealOwnerId()
        );

        return dto;
    }

    private List<OpportunityDealResponse> mapSalesforceDeals(
            List<OpportunityDealResponse> sourceDeals,
            List<OpportunityDealResponse> targetDeals,
            Long sourceOrganizationId,
            Long targetOrganizationId) {

        log.info("Started Salesforce deal-to-deal mapping based on website");

        if (CollectionUtils.isEmpty(sourceDeals) || CollectionUtils.isEmpty(targetDeals)) {
            return Collections.emptyList();
        }

        SalesforceTokenResponse targetToken =
                getSalesforceToken(targetOrganizationId);

        Map<String, OpportunityDealResponse> targetDealMap =
                mapInParallel(
                        targetDeals,
                        targetDeal -> {

                            if (targetDeal == null || targetDeal.getDealId() == null) {
                                return null;
                            }

                            try {

                                String website = salesforceDataMappingService.getWebsiteByOpportunityId(
                                        targetToken.instanceUrl(),
                                        targetDeal.getDealId(),
                                        targetToken.accessToken()
                                );

                                if (!isValidWebsite(website)) {
                                    return null;
                                }

                                targetDeal.setWebsite(website);
                                targetDeal.setTargetOwnerId(targetDeal.getDealOwner());

                                return targetDeal;

                            } catch (Exception ex) {
                                log.error("Unable to fetch website for target opportunity {}", targetDeal.getDealId(), ex);
                                return null;
                            }
                        }).stream()
                        .collect(Collectors.toMap(
                                deal -> normalizeWebsite(deal.getWebsite()),
                                Function.identity(),
                                (existing, replacement) -> existing
                        ));


        SalesforceTokenResponse sourceToken =
                getSalesforceToken(sourceOrganizationId);

        List<OpportunityDealResponse> matchedDeals =
                mapInParallel(
                        sourceDeals,
                        sourceDeal -> {

            if (sourceDeal == null || sourceDeal.getDealId() == null) {
                return null;
            }

            try {

                String website = salesforceDataMappingService.getWebsiteByOpportunityId(
                        sourceToken.instanceUrl(),
                        sourceDeal.getDealId(),
                        sourceToken.accessToken()
                );

                if (!isValidWebsite(website)) {
                    return null;
                }

                OpportunityDealResponse targetDeal =
                        targetDealMap.get(normalizeWebsite(website));

                if (targetDeal == null) {
                    return null;
                }

                DealActionType actionType =
                        resolveDealAction(sourceDeal, targetDeal);

                sourceDeal.setWebsite(website);
                sourceDeal.setDealActionType(actionType);
                sourceDeal.setTargetPartnerDealId(targetDeal.getDealId());
                sourceDeal.setOwnerId(sourceDeal.getDealOwner());
                sourceDeal.setTargetOwnerId(targetDeal.getDealOwner());

                log.info(
                        "Salesforce deal matched. source={}, target={}, website={}",
                        sourceDeal.getDealId(),
                        targetDeal.getDealId(),
                        website
                );

                return sourceDeal;

            } catch (Exception ex) {

                log.error("Unable to fetch website for source opportunity {}", sourceDeal.getDealId(), ex);
                return null;

            }
        });

        log.info("Salesforce mapping completed. matched={}", matchedDeals.size());

        return matchedDeals;
    }

    public SalesforceDealData getSalesforceDealData(
            Long organizationId,
            String opportunityId) {

        SalesforceTokenResponse token =
                getSalesforceToken(organizationId);

        return salesforceDataMappingService.getSalesforceDealData(
                token.instanceUrl(),
                opportunityId,
                token.accessToken()
        );
    }

    private SalesforceTokenResponse getSalesforceToken(Long organizationId) {

        String refreshToken =
                integrationRepository
                        .findByOrganizationIdAndIntegrationTypeAndIsConnectedAndRefreshTokenIsNotNull(
                                organizationId,
                                SALESFORCE,
                                true)
                        .stream()
                        .findFirst()
                        .map(IntegrationDetails::getRefreshToken)
                        .orElseThrow(() ->
                                new IllegalStateException("No Salesforce refresh token found"));

        return salesforceTokenCache.get(
                String.valueOf(organizationId),
                key -> salesforceAuthService.refreshAccessToken(refreshToken)
        );
    }

    private <T, R> List<R> mapInParallel(
            Collection<T> items,
            Function<T, R> mapper
    ) {

        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }

        List<CompletableFuture<R>> futures =
                items.stream()
                        .map(item ->
                                CompletableFuture.supplyAsync(
                                        () -> mapper.apply(item),
                                        sharedAccountsExecutor
                                )
                        )
                        .toList();

        return futures.stream()
                .map(future -> {
                    try {
                        return future.join();
                    } catch (Exception ex) {
                        log.error("Parallel shared-account task failed", ex);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

}

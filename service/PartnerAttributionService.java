package com.sharkdom.partnerattribution.service;

import com.sharkdom.entity.ai.OverlapRecordEntity;
import com.sharkdom.entity.ai.OverlapRecordFieldEntity;
import com.sharkdom.model.ai.RecordType;
import com.sharkdom.partnerattribution.dto.SharedAccountResponse;
import com.sharkdom.partnerattribution.enums.OverlapType;
import com.sharkdom.repository.ai.OverlapRecordsRepository;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerAttributionService {

    private final OverlapRecordsRepository overlapRecordsRepository;
    private final ContactAssociationService contactAssociationService;
    private final DealAssociationService dealAssociationService;
    private final CompanyAssociationService companyAssociationService;
    private final PartnerAttributionResolverService attributionResolverService;

    public List<SharedAccountResponse> getPartnerAttribution(
            Long partnerOrgId
    ) {
        log.info(
                "Started partner attribution for partnerOrgId={}",
                partnerOrgId
        );

        Long sourceOrgId = Util.getOrgIdFromToken();

        Map<RecordType, List<OverlapRecordFieldEntity>> sourceRecords =
                fetchRecordsGroupedByType(sourceOrgId);

        Map<RecordType, List<OverlapRecordFieldEntity>> partnerRecords =
                fetchRecordsGroupedByType(partnerOrgId);

        List<SharedAccountResponse> accounts = new ArrayList<>();

        for (RecordType sourceType : RecordType.values()) {
            for (RecordType targetType : RecordType.values()) {

                List<OverlapRecordFieldEntity> overlaps =
                        getOverlaps(
                                sourceType,
                                targetType,
                                sourceRecords,
                                partnerRecords
                        );

                if (overlaps.isEmpty()) {
                    continue;
                }

                PartnerAttributionResponse attribution =
                        attributionResolverService.resolve(
                                sourceType,
                                targetType
                        );

                for (OverlapRecordFieldEntity record : overlaps) {
                    accounts.add(
                            buildSharedAccount(
                                    record,
                                    attribution
                            )
                    );
                }
            }
        }

        log.info(
                "Completed partner attribution. totalAccounts={}",
                accounts.size()
        );

        return accounts;
    }

    private SharedAccountResponse buildSharedAccount(
            OverlapRecordFieldEntity record,
            PartnerAttributionResponse attribution
    ) {
        return SharedAccountResponse.builder()
                .accountName(record.getCompanyName())
                .website(record.getWebsite())
                .overlapType(resolveOverlapType(attribution))
                .opportunityScore(generateOpportunityScore())
                .yourStage(record.getDealStage())
                .partnerStage(resolvePartnerStage(record))
                .estimatedAcv(record.getAmountAcv())
                .action(attribution.getAction())
                .motion(attribution.getMotion())
                .build();
    }

    private OverlapType resolveOverlapType(
            PartnerAttributionResponse attribution
    ) {
        switch (attribution.getAction()) {
            case START_CO_SELL:
                return OverlapType.CO_SELL_READY;

            case REQUEST_INTRO:
                return OverlapType.HOT_OVERLAP;

            case ADD_TO_PIPELINE:
                return OverlapType.MONITOR;

            default:
                return OverlapType.LOW_PRIORITY;
        }
    }

    private String resolvePartnerStage(
            OverlapRecordFieldEntity record
    ) {
        if (record.getDealStage() != null) {
            return record.getDealStage();
        }

        return "Not in CRM";
    }

    private Integer generateOpportunityScore() {
        return new Random().nextInt(100);
    }

    private Map<RecordType, List<OverlapRecordFieldEntity>> fetchRecordsGroupedByType(
            Long orgId
    ) {
        List<OverlapRecordEntity> overlapRecords =
                overlapRecordsRepository.findByOrganizationId(orgId);

        Map<RecordType, List<OverlapRecordFieldEntity>> grouped =
                new EnumMap<>(RecordType.class);

        for (OverlapRecordEntity record : overlapRecords) {
            grouped.computeIfAbsent(
                    record.getRecordType(),
                    key -> new ArrayList<>()
            ).addAll(record.getFields());
        }

        return grouped;
    }

    private List<OverlapRecordFieldEntity> getOverlaps(
            RecordType sourceType,
            RecordType targetType,
            Map<RecordType, List<OverlapRecordFieldEntity>> sourceRecords,
            Map<RecordType, List<OverlapRecordFieldEntity>> partnerRecords
    ) {
        List<OverlapRecordFieldEntity> sourceList =
                sourceRecords.getOrDefault(sourceType, List.of());

        List<OverlapRecordFieldEntity> targetList =
                partnerRecords.getOrDefault(targetType, List.of());

        if (sourceType == RecordType.CONTACTS &&
                targetType == RecordType.CONTACTS) {
            return contactAssociationService.mapContactToContact(sourceList, targetList);
        }

        if (sourceType == RecordType.CONTACTS &&
                targetType == RecordType.DEALS) {
            return contactAssociationService.mapContactToDeal(sourceList, targetList);
        }

        if (sourceType == RecordType.CONTACTS &&
                targetType == RecordType.COMPANIES) {
            return contactAssociationService.mapContactToCompany(sourceList, targetList);
        }

        if (sourceType == RecordType.DEALS &&
                targetType == RecordType.DEALS) {
            return dealAssociationService.mapDealToDeal(sourceList, targetList);
        }

        if (sourceType == RecordType.DEALS &&
                targetType == RecordType.COMPANIES) {
            return dealAssociationService.mapDealToCompany(sourceList, targetList);
        }

        if (sourceType == RecordType.DEALS &&
                targetType == RecordType.CONTACTS) {
            return dealAssociationService.mapDealToContact(sourceList, targetList);
        }

        if (sourceType == RecordType.COMPANIES &&
                targetType == RecordType.CONTACTS) {
            return companyAssociationService.mapCompanyToContact(sourceList, targetList);
        }

        if (sourceType == RecordType.COMPANIES &&
                targetType == RecordType.DEALS) {
            return companyAssociationService.mapCompanyToDeal(sourceList, targetList);
        }

        if (sourceType == RecordType.COMPANIES &&
                targetType == RecordType.COMPANIES) {
            return companyAssociationService.mapCompanyToCompany(sourceList, targetList);
        }

        return List.of();
    }
}
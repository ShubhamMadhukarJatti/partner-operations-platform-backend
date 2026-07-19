package com.sharkdom.partnerattribution.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.PartnerDataFields;
import com.sharkdom.entity.ai.OverlapRecordEntity;
import com.sharkdom.entity.ai.OverlapRecordFieldEntity;
import com.sharkdom.entity.ai.PartnerDataPermissionDetails;
import com.sharkdom.entity.ai.PartnerDataPermissionEntity;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.model.ai.PartnerDataPermission;
import com.sharkdom.model.ai.RecordType;
import com.sharkdom.model.organizatiocollaboration.CollaborationCategory;
import com.sharkdom.repository.ai.OverlapRecordsRepository;
import com.sharkdom.repository.ai.PartnerDataPermissionRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.service.organizationcollaboration.OrganizationCollaborationService;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerDataPipelineMappingService {

    private final OverlapRecordsRepository overlapRecordsRepository;
    private final PartnerDataPermissionRepository partnerDataPermissionRepository;
    private final OrganizationCollaborationService organizationCollaborationService;
    private final OrganizationRepository organizationRepository;

    private static final String NOT_SHARED = "NOT_SHARED";

    public Map<String, Integer> getOverlapRecords(Long orgBId) {
        log.info("Started overlap record calculation for partner organizationId={}", orgBId);

        Long orgAId = Util.getOrgIdFromToken();
        log.info("Fetched source organizationId={} from token", orgAId);

        Map<RecordType, List<OverlapRecordFieldEntity>> orgARecords =
                fetchRecordsGroupedByType(orgAId);

        Map<RecordType, List<OverlapRecordFieldEntity>> orgBRecords =
                fetchRecordsGroupedByType(orgBId);

        log.info("Fetched records for both organizations. sourceOrg={}, partnerOrg={}", orgAId, orgBId);

        Map<String, Integer> overlapMatrix =
                buildOverlapMatrix(orgARecords, orgBRecords);

        log.info("Completed overlap record calculation for partner organizationId={}", orgBId);

        return overlapMatrix;
    }

    private Map<String, Integer> buildOverlapMatrix(
            Map<RecordType, List<OverlapRecordFieldEntity>> orgARecords,
            Map<RecordType, List<OverlapRecordFieldEntity>> orgBRecords
    ) {
        log.debug("Started building overlap matrix");

        Map<String, Integer> overlapMatrix = new HashMap<>();

        for (RecordType sourceType : RecordType.values()) {
            for (RecordType targetType : RecordType.values()) {

                List<OverlapRecordFieldEntity> sourceRecords =
                        orgARecords.getOrDefault(sourceType, Collections.emptyList());

                List<OverlapRecordFieldEntity> targetRecords =
                        orgBRecords.getOrDefault(targetType, Collections.emptyList());

                log.debug(
                        "Calculating overlap for sourceType={} targetType={}",
                        sourceType,
                        targetType
                );

                int overlapCount = countOverlap(sourceRecords, targetRecords);

                String matrixKey = sourceType.name() + "_" + targetType.name();
                overlapMatrix.put(matrixKey, overlapCount);

                log.info(
                        "Overlap calculated for {} -> {} = {}",
                        sourceType,
                        targetType,
                        overlapCount
                );
            }
        }

        log.debug("Completed overlap matrix creation");
        return overlapMatrix;
    }

    private Map<RecordType, List<OverlapRecordFieldEntity>> fetchRecordsGroupedByType(Long orgId) {
        log.debug("Fetching overlap records for organizationId={}", orgId);

        List<OverlapRecordEntity> overlapRecords =
                overlapRecordsRepository.findByOrganizationId(orgId);

        log.info("Fetched {} overlap records for organizationId={}", overlapRecords.size(), orgId);

        Map<RecordType, List<OverlapRecordFieldEntity>> groupedRecords =
                new EnumMap<>(RecordType.class);

        for (OverlapRecordEntity overlapRecord : overlapRecords) {
            RecordType recordType = overlapRecord.getRecordType();
            List<OverlapRecordFieldEntity> recordFields = overlapRecord.getFields();

            log.debug(
                    "Processing recordType={} with {} fields",
                    recordType,
                    recordFields.size()
            );

            groupedRecords
                    .computeIfAbsent(recordType, key -> new ArrayList<>())
                    .addAll(recordFields);
        }

        log.debug("Completed grouping records for organizationId={}", orgId);

        return groupedRecords;
    }

    private int countOverlap(
            List<OverlapRecordFieldEntity> sourceRecords,
            List<OverlapRecordFieldEntity> targetRecords
    ) {
        log.debug(
                "Started overlap comparison. sourceSize={}, targetSize={}",
                sourceRecords.size(),
                targetRecords.size()
        );

        Set<String> sourceEmails = extractEmails(sourceRecords);
        Set<String> sourceDomains = extractDomains(sourceRecords);

        int overlapCount = 0;

        for (OverlapRecordFieldEntity targetRecord : targetRecords) {
            boolean emailMatched = isEmailMatched(targetRecord, sourceEmails);
            boolean domainMatched = isDomainMatched(targetRecord, sourceDomains);

            if (emailMatched || domainMatched) {
                overlapCount++;
            }
        }

        log.debug("Completed overlap comparison. overlapCount={}", overlapCount);

        return overlapCount;
    }

    private Set<String> extractEmails(List<OverlapRecordFieldEntity> records) {
        log.debug("Extracting emails from records. count={}", records.size());

        return records.stream()
                .map(OverlapRecordFieldEntity::getContactEmail)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private Set<String> extractDomains(List<OverlapRecordFieldEntity> records) {
        log.debug("Extracting domains from records. count={}", records.size());

        return records.stream()
                .map(OverlapRecordFieldEntity::getDomain)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private boolean isEmailMatched(
            OverlapRecordFieldEntity targetRecord,
            Set<String> sourceEmails
    ) {
        if (targetRecord.getContactEmail() == null) {
            return false;
        }

        return sourceEmails.contains(targetRecord.getContactEmail().toLowerCase());
    }

    private boolean isDomainMatched(
            OverlapRecordFieldEntity targetRecord,
            Set<String> sourceDomains
    ) {
        if (targetRecord.getDomain() == null) {
            return false;
        }

        return sourceDomains.contains(targetRecord.getDomain().toLowerCase());
    }

    public Map<String, Object> getPartnerDataWithPermissions(Long partnerId, String typeCombination) {
        log.info(
                "Started partner data fetch. partnerId={}, typeCombination={}",
                partnerId,
                typeCombination
        );

        Long organizationId = Util.getOrgIdFromToken();

        log.debug("Fetched organizationId={} from token", organizationId);

        CollaborationCategory collaborationCategory =
                organizationCollaborationService.getCollaborationCategory(partnerId);

        log.info(
                "Fetched collaboration category. organizationId={}, partnerId={}, category={}",
                organizationId,
                partnerId,
                collaborationCategory
        );

        PartnerDataPermissionEntity permissions =
                partnerDataPermissionRepository
                        .findByOrganizationIdAndCollaborationCategory(
                                organizationId,
                                collaborationCategory
                        )
                        .orElse(createDefaultPermissionEntity(
                                organizationId,
                                collaborationCategory
                        ));

        if (permissions.getPermissions() == null ||
                permissions.getPermissions().isEmpty()) {

            log.warn(
                    "No permissions found. Applying default FULL_ACCESS for organizationId={}",
                    organizationId
            );

            permissions.setPermissions(createDefaultPermissions());
        }

        Map<String, Object> response = new HashMap<>();

        if (typeCombination != null && !typeCombination.isEmpty()) {
            handleSpecificCombination(
                    partnerId,
                    typeCombination,
                    organizationId,
                    permissions,
                    response
            );
        } else {
            handleFullMatrix(
                    partnerId,
                    organizationId,
                    permissions,
                    response
            );
        }

        response.put(
                "organizationName",
                organizationRepository.findNameById(organizationId)
        );

        response.put(
                "partnerName",
                organizationRepository.findNameById(partnerId)
        );

        log.info("Completed partner data fetch for partnerId={}", partnerId);

        return response;
    }

    private void handleSpecificCombination(
            Long partnerId,
            String typeCombination,
            Long organizationId,
            PartnerDataPermissionEntity permissions,
            Map<String, Object> response
    ) {
        log.debug("Processing specific typeCombination={}", typeCombination);

        String[] types = typeCombination.split("_");

        if (types.length != 2) {
            log.error("Invalid type combination received={}", typeCombination);
            throw new SharkdomException(ErrorMessages.SH95);
        }

        RecordType typeA = RecordType.valueOf(types[0]);
        RecordType typeB = RecordType.valueOf(types[1]);

        PartnerDataPermissionDetails permissionDetails =
                permissions.getPermissions().get(typeA);

        if (permissionDetails == null) {
            log.error("Permission not found for recordType={}", typeA);
            throw new SharkdomException(ErrorMessages.SH96, typeA);
        }

        log.info(
                "Permission found for recordType={} accessType={}",
                typeA,
                permissionDetails.getAccessType()
        );

        switch (permissionDetails.getAccessType()) {
            case HIDDEN:
                log.warn("Access hidden for recordType={}", typeA);
                response.put(PartnerDataFields.MESSAGE, "Data access is hidden");
                break;

            case ONLY_COUNT:
                log.info("Returning only overlap count");
                response.put(
                        PartnerDataFields.COUNTS,
                        getOverlapRecords(partnerId)
                );
                break;

            case PARTIAL:
            case FULL_ACCESS:
                log.info(
                        "Returning data for typeA={} typeB={}",
                        typeA,
                        typeB
                );

                Map<RecordType, List<OverlapRecordFieldEntity>> orgARecords =
                        fetchRecordsGroupedByType(organizationId);

                Map<RecordType, List<OverlapRecordFieldEntity>> orgBRecords =
                        fetchRecordsGroupedByType(partnerId);

                Map<String, Object> data =
                        createCombinationData(
                                typeA,
                                typeB,
                                orgARecords,
                                orgBRecords,
                                permissionDetails,
                                getOverlapRecords(partnerId)
                        );

                response.put(PartnerDataFields.DATA, data);
                break;
        }
    }

    private void handleFullMatrix(
            Long partnerId,
            Long organizationId,
            PartnerDataPermissionEntity permissions,
            Map<String, Object> response
    ) {
        log.info("Building full matrix response for partnerId={}", partnerId);

        Map<String, Object> matrix = new HashMap<>();
        Map<String, Integer> overlapCounts = getOverlapRecords(partnerId);

        Map<RecordType, List<OverlapRecordFieldEntity>> orgARecords =
                fetchRecordsGroupedByType(organizationId);

        Map<RecordType, List<OverlapRecordFieldEntity>> orgBRecords =
                fetchRecordsGroupedByType(partnerId);

        for (RecordType sourceType : RecordType.values()) {
            PartnerDataPermissionDetails permissionDetails =
                    permissions.getPermissions().get(sourceType);

            if (permissionDetails == null ||
                    permissionDetails.getAccessType() ==
                            PartnerDataPermission.AccessType.HIDDEN) {

                log.warn(
                        "Skipping sourceType={} due to hidden/no permission",
                        sourceType
                );
                continue;
            }

            Map<String, Object> sourceMatrix = new HashMap<>();

            for (RecordType targetType : RecordType.values()) {
                sourceMatrix.put(
                        targetType.name(),
                        createCombinationData(
                                sourceType,
                                targetType,
                                orgARecords,
                                orgBRecords,
                                permissionDetails,
                                overlapCounts
                        )
                );
            }

            matrix.put(sourceType.name(), sourceMatrix);
        }

        response.put(PartnerDataFields.MATRIX, matrix);

        log.info("Completed full matrix response");
    }

    private PartnerDataPermissionEntity createDefaultPermissionEntity(
            Long organizationId,
            CollaborationCategory collaborationCategory
    ) {
        log.debug("Creating default permission entity");

        return PartnerDataPermissionEntity.builder()
                .organizationId(organizationId)
                .collaborationCategory(collaborationCategory)
                .permissions(new HashMap<>())
                .build();
    }

    private Map<RecordType, PartnerDataPermissionDetails> createDefaultPermissions() {
        Map<RecordType, PartnerDataPermissionDetails> defaultPermissions =
                new HashMap<>();

        for (RecordType recordType : RecordType.values()) {
            log.debug("Applying FULL_ACCESS for recordType={}", recordType);

            defaultPermissions.put(
                    recordType,
                    PartnerDataPermissionDetails.builder()
                            .recordType(recordType)
                            .accessType(
                                    PartnerDataPermission.AccessType.FULL_ACCESS
                            )
                            .sharedFields(Set.of())
                            .build()
            );
        }

        return defaultPermissions;
    }

    private Map<String, Object> createCombinationData(
            RecordType typeA,
            RecordType typeB,
            Map<RecordType, List<OverlapRecordFieldEntity>> orgARecords,
            Map<RecordType, List<OverlapRecordFieldEntity>> orgBRecords,
            PartnerDataPermissionDetails permissionDetails,
            Map<String, Integer> overlapCounts
    ) {
        log.info(
                "Creating combination data for typeA={} typeB={}",
                typeA,
                typeB
        );

        List<OverlapRecordFieldEntity> aList =
                orgARecords.getOrDefault(typeA, List.of());

        List<OverlapRecordFieldEntity> bList =
                orgBRecords.getOrDefault(typeB, List.of());

        log.debug(
                "Raw records loaded. sourceCount={}, partnerCount={}",
                aList.size(),
                bList.size()
        );

        List<OverlapRecordFieldEntity> overlappingBRecords =
                getOverlappingRecords(aList, bList);

        List<OverlapRecordFieldEntity> overlappingARecords =
                getOverlappingRecords(bList, aList);

        if (permissionDetails.getAccessType() ==
                PartnerDataPermission.AccessType.PARTIAL) {

            log.debug(
                    "Applying partial field filtering. sharedFields={}",
                    permissionDetails.getSharedFields().size()
            );

            overlappingARecords =
                    filterFields(
                            overlappingARecords,
                            permissionDetails.getSharedFields()
                    );

            overlappingBRecords =
                    filterFields(
                            overlappingBRecords,
                            permissionDetails.getSharedFields()
                    );
        }

        List<Map<String, Object>> combinedRecords =
                combineRecords(overlappingARecords, overlappingBRecords);

        Map<String, Object> combinationData = new HashMap<>();

        combinationData.put(PartnerDataFields.RECORDS, combinedRecords);
        combinationData.put(
                PartnerDataFields.OVERLAP_COUNT,
                overlapCounts.getOrDefault(
                        typeA.name() + "_" + typeB.name(),
                        0
                )
        );
        combinationData.put("raw_records_A", aList.size());
        combinationData.put("raw_records_B", bList.size());
        combinationData.put("raw_records_sum", aList.size() + bList.size());
        combinationData.put("overlap_customer_count", combinedRecords.size());

        log.info(
                "Completed combination data creation for typeA={} typeB={}",
                typeA,
                typeB
        );

        return combinationData;
    }

    private List<Map<String, Object>> combineRecords(
            List<OverlapRecordFieldEntity> orgRecords,
            List<OverlapRecordFieldEntity> partnerRecords
    ) {
        log.debug("Combining overlapping records");

        Map<String, Map<String, Object>> recordMap = new HashMap<>();

        for (OverlapRecordFieldEntity orgRecord : orgRecords) {
            String key = generateRecordKey(orgRecord);

            if (key != null) {
                Map<String, Object> record = new HashMap<>();
                record.put("organization_record", orgRecord);
                recordMap.put(key, record);
            }
        }

        for (OverlapRecordFieldEntity partnerRecord : partnerRecords) {
            String key = generateRecordKey(partnerRecord);

            if (key != null) {
                Map<String, Object> record =
                        recordMap.getOrDefault(key, new HashMap<>());

                record.put("partner_record", partnerRecord);
                recordMap.putIfAbsent(key, record);
            }
        }

        log.debug("Completed record combination. total={}", recordMap.size());

        return new ArrayList<>(recordMap.values());
    }

    private String generateRecordKey(OverlapRecordFieldEntity record) {
        if (record.getContactEmail() != null) {
            return record.getContactEmail().toLowerCase();
        }

        if (record.getDomain() != null) {
            return record.getDomain().toLowerCase();
        }

        return null;
    }

    private List<OverlapRecordFieldEntity> filterFields(
            List<OverlapRecordFieldEntity> records,
            Set<String> sharedFields
    ) {
        log.debug(
                "Filtering fields. recordsCount={}, sharedFieldsCount={}",
                records.size(),
                sharedFields.size()
        );

        return records.stream().map(field -> {
            OverlapRecordFieldEntity filtered = new OverlapRecordFieldEntity();

            filtered.setName(sharedFields.contains(PartnerDataFields.NAME) ? field.getName() : NOT_SHARED);
            filtered.setCompanyName(sharedFields.contains(PartnerDataFields.COMPANY_NAME) ? field.getCompanyName() : NOT_SHARED);
            filtered.setContactEmail(sharedFields.contains(PartnerDataFields.CONTACT_EMAIL) ? field.getContactEmail() : NOT_SHARED);
            filtered.setDomain(sharedFields.contains(PartnerDataFields.DOMAIN) ? field.getDomain() : NOT_SHARED);
            filtered.setWebsite(sharedFields.contains(PartnerDataFields.WEBSITE) ? field.getWebsite() : NOT_SHARED);

            return filtered;
        }).toList();
    }

    public List<OverlapRecordFieldEntity> getOverlappingRecords(
            List<OverlapRecordFieldEntity> listA,
            List<OverlapRecordFieldEntity> listB
    ) {
        log.info(
                "Started finding overlapping records. sourceCount={}, targetCount={}",
                listA.size(),
                listB.size()
        );

        Set<String> emailsA = listA.stream()
                .map(OverlapRecordFieldEntity::getContactEmail)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> domainsA = listA.stream()
                .map(OverlapRecordFieldEntity::getDomain)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        log.debug(
                "Prepared matching sets. emails={}, domains={}",
                emailsA.size(),
                domainsA.size()
        );

        List<OverlapRecordFieldEntity> result =
                listB.stream()
                        .filter(record ->
                                (record.getContactEmail() != null &&
                                        emailsA.contains(
                                                record.getContactEmail().toLowerCase()
                                        ))
                                        ||
                                        (record.getDomain() != null &&
                                                domainsA.contains(
                                                        record.getDomain().toLowerCase()
                                                ))
                        )
                        .collect(Collectors.toList());

        log.info(
                "Completed overlapping records search. matchedRecords={}",
                result.size()
        );

        return result;
    }
}
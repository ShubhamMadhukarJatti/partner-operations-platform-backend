package com.sharkdom.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.entity.ai.OverlapRecordFieldResponse;
import com.sharkdom.entity.ai.PersonaVersionResponse;
import com.sharkdom.entity.ai.PersonaVersioning;

import com.sharkdom.model.ai.OverlapFrequency;
import com.sharkdom.model.ai.PersonaMode;
import com.sharkdom.model.ai.RecordType;
import com.sharkdom.repository.ai.OverlapRecordsRepository;
import com.sharkdom.repository.ai.PersonaVersioningRepository;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonaVersioningService {

    private final PersonaVersioningRepository repository;
    private final HubspotService hubspotService;
    private final OrganizationRepository organizationRepository;
    private final OverlapRecordsRepository overlapRecordsRepository;
    private final IntegrationRepository integrationRepository;
    private final ObjectMapper objectMapper;

    public void createPersonaVersioningForOrg(Long orgId) {

        long startTime = System.currentTimeMillis();

        log.info("Persona versioning started for orgId={}", orgId);

        try {

            var overlapRecordEntity = overlapRecordsRepository
                    .findByOrganizationId(orgId)
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (overlapRecordEntity == null) {

                log.warn("No overlap record found for orgId={}. Skipping persona version creation.", orgId);

                return;
            }

            OverlapFrequency frequency = overlapRecordEntity.getFrequency();

            if (frequency == null) {

                log.warn("Overlap frequency is null for orgId={}. Skipping persona version creation.", orgId);

                return;
            }

            if (frequency.getDuration().isZero()) {

                log.debug("Overlap frequency is NONE for orgId={}. No scheduling required.", orgId);

                return;
            }

            log.debug("Overlap frequency for orgId={} is {} (duration={} ms)",
                    orgId,
                    frequency.name(),
                    frequency.getDuration().toMillis());

            PersonaVersioning lastVersion =
                    repository.findTopByOrgIdAndPersonaModeOrderByVersionDesc(
                            orgId,
                            PersonaMode.HUBSPOT
                    ).orElse(null);

            Date baseTime;

            if (lastVersion == null) {

                baseTime = overlapRecordEntity.getCreationTimestamp();

                log.info("No existing persona version found. Using overlapRecord creationTimestamp={} for orgId={}",
                        baseTime,
                        orgId);

            } else {

                baseTime = lastVersion.getCreationTimestamp();

                log.info("Last persona version found with version={} and creationTimestamp={} for orgId={}",
                        lastVersion.getVersion(),
                        baseTime,
                        orgId);
            }

            long nextRunMillis =
                    baseTime.getTime() + frequency.getDuration().toMillis();

            long currentMillis = System.currentTimeMillis();

            if (currentMillis < nextRunMillis) {

                long remainingMillis = nextRunMillis - currentMillis;

                log.debug("Persona version creation skipped for orgId={}. Next eligible run in {} ms",
                        orgId,
                        remainingMillis);

                return;
            }

            log.info("Persona version eligible for creation for orgId={}", orgId);

            Map<String, String> mapping = getFieldMapping();

            List<String> properties =
                    mapping.values().stream()
                            .filter(v -> !"dont_import".equals(v))
                            .toList();

            log.debug("Fetching HubSpot details for orgId={} with {} properties",
                    orgId,
                    properties.size());

            Map<Object, Object> response =
                    hubspotService.getDetailsForVersioning(orgId, properties);

            if (response == null || response.isEmpty()) {

                log.warn("HubSpot response is empty for orgId={}. Persona version will still be created with empty data.",
                        orgId);
            }

            Map<String, Object> safeResponse = convertToSafeMap(response);

            PersonaVersioning savedVersion =
                    createNewVersion(
                            orgId,
                            PersonaMode.HUBSPOT,
                            overlapRecordEntity.getRecordType(),
                            overlapRecordEntity.getFrequency(),
                            safeResponse
                    );

            long executionTime = System.currentTimeMillis() - startTime;

            log.info("Persona version created successfully for orgId={}, version={}, executionTime={} ms",
                    orgId,
                    savedVersion.getVersion(),
                    executionTime);

        } catch (Exception ex) {

            long executionTime = System.currentTimeMillis() - startTime;

            log.error("Persona version creation failed for orgId={}, executionTime={} ms, error={}",
                    orgId,
                    executionTime,
                    ex.getMessage(),
                    ex);

            throw ex;
        }
    }

    private Map<String, String> getFieldMapping() {
        return Map.of(
                "domain", "website",
                "name", "firstname",
                "companyName", "company",
                "contactEmail", "email",
                "dealStage", "dealstage",
                "creationDate", "createdate",
                "closeDate", "recent_deal_close_date",
                "subscribed", "hs_has_active_subscription",
                "ticketSize", "annualrevenue"
        );
    }

    public PersonaVersioning createNewVersion(
            Long orgId,
            PersonaMode personaMode,
            RecordType recordType,
            OverlapFrequency overlapFrequency,
            Map<String, Object> overlapRecord
    ) {

        Integer nextVersion = getNextVersion(orgId, personaMode);

        PersonaVersioning entity = new PersonaVersioning();

        entity.setOrgId(orgId);
        entity.setRecordType(recordType);
        entity.setOverlapFrequency(overlapFrequency);
        entity.setPersonaMode(personaMode);
        entity.setVersion(nextVersion);

        Integer versionId = generateNextVersionId(orgId, personaMode);
        entity.setVersionId(versionId);

        JsonNode jsonNode = objectMapper.valueToTree(overlapRecord);
        entity.setOverlapRecord(jsonNode);

        return repository.save(entity);
    }

    /**
     * Get latest version
     */
    public PersonaVersioning getLatestVersion(
            Long orgId,
            PersonaMode personaMode
    ) {
        return repository
                .findTopByOrgIdAndPersonaModeOrderByVersionDesc(orgId, personaMode)
                .orElse(null);
    }

    /**
     * Calculate next version automatically
     */
    private Integer getNextVersion(Long orgId, PersonaMode personaMode) {

        return repository
                .findTopByOrgIdAndPersonaModeOrderByVersionDesc(orgId, personaMode)
                .map(existing -> existing.getVersion() + 1)
                .orElse(1);

    }

    public void runScheduler() {

        log.info("PersonaVersioning Scheduler triggered at {}", new Date());

        var integrationDetailsList =
                integrationRepository
                        .findAllByIntegrationTypeAndIsConnectedTrueAndRefreshTokenIsNotNull(
                                IntegrationType.HUBSPOT
                        );

        log.info("Total organizations found: {}", integrationDetailsList.size());

        for (var integration : integrationDetailsList) {

            Long orgId = integration.getOrganizationId();

            log.info("Checking orgId={}", orgId);

            // No need to query again, already filtered
            log.info("Integration found for orgId={}, creating persona version", orgId);

            createPersonaVersioningForOrg(orgId);
        }
    }

    public void runSchedulerForSalesforce() {

        log.info("PersonaVersioning Scheduler triggered at {}", new Date());

        var integrationDetailsList =
                integrationRepository
                        .findAllByIntegrationTypeAndIsConnectedTrueAndRefreshTokenIsNotNull(
                                IntegrationType.SALESFORCE
                        );

        log.info("Total organizations found: {}", integrationDetailsList.size());

        for (var integration : integrationDetailsList) {

            Long orgId = integration.getOrganizationId();

            log.info("Checking orgId={}", orgId);

            // No need to query again, already filtered
            log.info("Integration found for orgId={}, creating persona version", orgId);

            createPersonaVersioningForOrg(orgId);
        }
    }

    /**
     * Manual Persona Version Creation
     * - No frequency check
     * - No scheduler dependency
     * - Direct version creation
     */
    public PersonaVersioning createPersonaVersioningManual(Long orgId) {

        long startTime = System.currentTimeMillis();
        log.info("Manual persona versioning started for orgId={}", orgId);

        try {

            var overlapRecordEntity = overlapRecordsRepository
                    .findByOrganizationId(orgId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() ->
                            new RuntimeException("No overlap record found for orgId=" + orgId));

            Map<String, String> mapping = getFieldMapping();

            List<String> properties = mapping.values()
                    .stream()
                    .filter(v -> !"dont_import".equals(v))
                    .toList();

            log.debug("Fetching HubSpot details manually for orgId={} with {} properties",
                    orgId,
                    properties.size());

            Map<Object, Object> response =
                    hubspotService.getDetailsForVersioning(orgId, properties);

            if (response == null || response.isEmpty()) {
                log.warn("HubSpot response is empty for orgId={}. Version will be created with empty data.",
                        orgId);
            }

            Map<String, Object> safeResponse = convertToSafeMap(response);

            PersonaVersioning savedVersion =
                    createNewVersion(
                            orgId,
                            PersonaMode.HUBSPOT,
                            overlapRecordEntity.getRecordType(),
                            OverlapFrequency.NONE, // since manual
                            safeResponse
                    );

            long executionTime = System.currentTimeMillis() - startTime;

            log.info("Manual persona version created successfully for orgId={}, version={}, executionTime={} ms",
                    orgId,
                    savedVersion.getVersion(),
                    executionTime);

            return savedVersion;

        } catch (Exception ex) {

            long executionTime = System.currentTimeMillis() - startTime;

            log.error("Manual persona version creation failed for orgId={}, executionTime={} ms, error={}",
                    orgId,
                    executionTime,
                    ex.getMessage(),
                    ex);

            throw ex;
        }
    }

    private Map<String, Object> convertToSafeMap(Map<Object, Object> input) {

        Map<String, Object> safeMap = new HashMap<>();

        if (input == null) {
            return safeMap;
        }

        for (Map.Entry<Object, Object> entry : input.entrySet()) {

            String key = String.valueOf(entry.getKey());
            Object safeValue = convertToSafeValue(entry.getValue());

            safeMap.put(key, safeValue);
        }

        return safeMap;
    }

    private Object convertToSafeValue(Object value) {

        if (value == null)
            return null;

        // primitives
        if (value instanceof String ||
                value instanceof Number ||
                value instanceof Boolean)
            return value;

        // Date conversion
        if (value instanceof Date date)
            return date.toInstant().toString();

        // Map recursive safe conversion
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> nestedSafe = new HashMap<>();
            map.forEach((k, v) ->
                    nestedSafe.put(String.valueOf(k), convertToSafeValue(v)));
            return nestedSafe;
        }

        // List recursive safe conversion
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(this::convertToSafeValue)
                    .toList();
        }

        // Hibernate entities or unknown objects → convert to String
        return value.toString();
    }

    public PersonaVersionResponse getVersionByNumber(
            Long orgId,
            PersonaMode personaMode,
            Integer version
    ) {

        PersonaVersioning entity =
                repository.findByOrgIdAndPersonaModeAndVersion(
                        orgId,
                        personaMode,
                        version
                ).orElseThrow(() ->
                        new RuntimeException("Persona version not found")
                );

        return new PersonaVersionResponse(
                entity.getOrgId(),
                entity.getPersonaMode(),
                entity.getVersion(),
                entity.getOverlapFrequency(),
                entity.getRecordType(),
                entity.getOverlapRecord()
        );
    }

    /**
     * Get all persona versions by orgId
     * Optional filter by personaMode
     */
    public List<PersonaVersionResponse> getAllVersionsByOrgId(
            Long orgId,
            PersonaMode personaMode
    ) {

        List<PersonaVersioning> versions;

        if (personaMode != null) {
            versions = repository
                    .findByOrgIdAndPersonaModeOrderByVersionDesc(
                            orgId,
                            personaMode
                    );
        } else {
            versions = repository
                    .findByOrgIdOrderByVersionDesc(orgId);
        }

        if (versions.isEmpty()) {
            throw new RuntimeException(
                    "No persona versions found for orgId=" + orgId
            );
        }

        return versions.stream()
                .map(entity -> new PersonaVersionResponse(
                        entity.getOrgId(),
                        entity.getPersonaMode(),
                        entity.getVersion(),
                        entity.getOverlapFrequency(),
                        entity.getRecordType(),
                        entity.getOverlapRecord()
                ))
                .toList();
    }

    private String getText(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull()
                ? node.get(field).asText()
                : null;
    }


    public List<OverlapRecordFieldResponse> getOverlapFieldsFromVersion(
            Long orgId,
            PersonaMode personaMode,
            Integer version
    ) {

        // Fetch version
        PersonaVersioning personaVersion = repository
                .findByOrgIdAndPersonaModeAndVersion(orgId, personaMode, version)
                .orElseThrow(() ->
                        new RuntimeException("Persona version not found"));

        JsonNode root = personaVersion.getOverlapRecord();

        List<OverlapRecordFieldResponse> responseList = new ArrayList<>();

        if (root == null || !root.has("results")) {
            return responseList;
        }

        // Loop through results
        for (JsonNode resultNode : root.get("results")) {

            JsonNode properties = resultNode.get("properties");

            if (properties == null) continue;

            OverlapRecordFieldResponse response =
                    new OverlapRecordFieldResponse(

                            getText(properties, "firstname"),
                            getText(properties, "company"),
                            getText(properties, "email"),
                            getText(properties, "website"),
                            getText(properties, "dealstage"),
                            getText(properties, "createdate"),
                            getText(properties, "recent_deal_close_date"),
                            getText(properties, "hs_has_active_subscription"),
                            getText(properties, "annualrevenue")
                    );

            responseList.add(response);
        }

        return responseList;
    }

//    public void createPersonaVersioningForOrgForSalesforce(Long orgId) {
//
//        long startTime = System.currentTimeMillis();
//
//        log.info("Persona versioning started for orgId={}", orgId);
//
//        try {
//
//            var overlapRecordEntity = overlapRecordsRepository
//                    .findByOrganizationId(orgId)
//                    .stream()
//                    .findFirst()
//                    .orElse(null);
//
//            if (overlapRecordEntity == null) {
//
//                log.warn("No overlap record found for orgId={}. Skipping persona version creation.", orgId);
//
//                return;
//            }
//
//            OverlapFrequency frequency = overlapRecordEntity.getFrequency();
//
//            if (frequency == null) {
//
//                log.warn("Overlap frequency is null for orgId={}. Skipping persona version creation.", orgId);
//
//                return;
//            }
//
//            if (frequency.getDuration().isZero()) {
//
//                log.debug("Overlap frequency is NONE for orgId={}. No scheduling required.", orgId);
//
//                return;
//            }
//
//            log.debug("Overlap frequency for orgId={} is {} (duration={} ms)",
//                    orgId,
//                    frequency.name(),
//                    frequency.getDuration().toMillis());
//
//            PersonaVersioning lastVersion =
//                    repository.findTopByOrgIdAndPersonaModeOrderByVersionDesc(
//                            orgId,
//                            PersonaMode.SALESFORCE
//                    ).orElse(null);
//
//            Date baseTime;
//
//            if (lastVersion == null) {
//
//                baseTime = overlapRecordEntity.getCreationTimestamp();
//
//                log.info("No existing persona version found. Using overlapRecord creationTimestamp={} for orgId={}",
//                        baseTime,
//                        orgId);
//
//            } else {
//
//                baseTime = lastVersion.getCreationTimestamp();
//
//                log.info("Last persona version found with version={} and creationTimestamp={} for orgId={}",
//                        lastVersion.getVersion(),
//                        baseTime,
//                        orgId);
//            }
//
//            long nextRunMillis =
//                    baseTime.getTime() + frequency.getDuration().toMillis();
//
//            long currentMillis = System.currentTimeMillis();
//
//            if (currentMillis < nextRunMillis) {
//
//                long remainingMillis = nextRunMillis - currentMillis;
//
//                log.debug("Persona version creation skipped for orgId={}. Next eligible run in {} ms",
//                        orgId,
//                        remainingMillis);
//
//                return;
//            }
//
//            log.info("Persona version eligible for creation for orgId={}", orgId);
//
//            Map<String, String> mapping = getFieldMapping();
//
//            List<String> properties =
//                    mapping.values().stream()
//                            .filter(v -> !"dont_import".equals(v))
//                            .toList();
//
//            log.debug("Fetching HubSpot details for orgId={} with {} properties",
//                    orgId,
//                    properties.size());
//
//            Map<Object, Object> response;
////                    hubspotService.getDetailsForVersioningSalesForce(orgId, properties);
//
//            if (response == null || response.isEmpty()) {
//
//                log.warn("Salesforce response is empty for orgId={}. Persona version will still be created with empty data.",
//                        orgId);
//            }
//
//            Map<String, Object> safeResponse = convertToSafeMap(response);
//
//            PersonaVersioning savedVersion =
//                    createNewVersion(
//                            orgId,
//                            PersonaMode.SALESFORCE,
//                            overlapRecordEntity.getRecordType(),
//                            overlapRecordEntity.getFrequency(),
//                            safeResponse
//                    );
//
//            long executionTime = System.currentTimeMillis() - startTime;
//
//            log.info("Persona version created successfully for orgId={}, version={}, executionTime={} ms",
//                    orgId,
//                    savedVersion.getVersion(),
//                    executionTime);
//
//        } catch (Exception ex) {
//
//            long executionTime = System.currentTimeMillis() - startTime;
//
//            log.error("Persona version creation failed for orgId={}, executionTime={} ms, error={}",
//                    orgId,
//                    executionTime,
//                    ex.getMessage(),
//                    ex);
//
//            throw ex;
//        }
//    }

private Integer generateNextVersionId(Long orgId, PersonaMode personaMode) {

    PersonaVersioning lastVersion =
            repository.findTopByOrgIdAndPersonaModeOrderByVersionDesc(orgId, personaMode)
                    .orElse(null);

    int base;

    // First version
    if (lastVersion == null || lastVersion.getVersionId() == null) {
        return 11111;
    }

    base = lastVersion.getVersionId();

    // random increment between 9 and 45
    int increment = new Random().nextInt(37) + 9;

    return base + increment;
}

}
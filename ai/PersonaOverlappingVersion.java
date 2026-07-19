package com.sharkdom.service.ai;
import com.sharkdom.entity.ai.OverlapRecordEntity;
import com.sharkdom.entity.ai.OverlapRecordFieldEntity;
import com.sharkdom.model.ai.*;
import com.sharkdom.repository.ai.OverlapRecordsRepository;
import com.sharkdom.salesforce.service.SalesforceService;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonaOverlappingVersion {

    private final OverlapRecordsRepository overlapRecordsRepository;
    private final PersonaService personaService;
    private final HubspotService hubspotService;
    private final SalesforceService salesforceService;
    private final PersonaDealsCompanyContactOverlapVersion personaOverlapping;
    private final ProspectPersonaService prospectPersonaService;
    private final CustomerPersonaService customerPersonaService;
    private final OpportunityPersonaService opportunityPersonaService;


    /**
     * CRON RUNS EVERY 1 HOUR
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void processOverlapVersioningJOB(){

        log.info("[CRON_START] Running overlap version cron");

        List<OverlapRecordEntity> records =
                overlapRecordsRepository.findLatestVersionPerOrgAndRecordType(RecordType.CUSTOMER);

        if(records == null || records.isEmpty()){
            log.info("[CRON_INFO] No overlap records found");
            return;
        }

        for(OverlapRecordEntity record : records){

            try{

                if(record == null){
                    continue;
                }

                if(record.getFrequency() == null ||
                        record.getFrequency() == OverlapFrequency.NONE){
                    continue;
                }

                if(shouldCreateNextVersion(record)){
                    createNextVersion(record);
                }

            }catch(Exception ex){
                log.error("[CRON_ERROR] Failed processing record id={}",
                        record.getId(), ex);
            }
        }

        log.info("[CRON_END] Overlap version cron finished");
    }

    private void createNextVersion(OverlapRecordEntity lastRecord){

        if(lastRecord == null){
            return;
        }

        try{

            Map<String,String> mapping = lastRecord.getFieldToColumnMapping();

            if(mapping == null || mapping.isEmpty()){
                log.warn("[MAPPING_EMPTY] orgId={}",
                        lastRecord.getOrganizationId());
                return;
            }

            List<OverlapRecordsField> fields;

            if(lastRecord.getSource() == PersonaMode.HUBSPOT){

                log.info("[V2_HUBSPOT_FLOW] orgId={}",
                        lastRecord.getOrganizationId());

                fields = personaOverlapping
                        .triggerForOrgAndReturnFields(lastRecord);
            }
            else if(lastRecord.getSource() == PersonaMode.SALESFORCE){

                log.info("[SALESFORCE_CALL] orgId={}",
                        lastRecord.getOrganizationId());

                fields = fetchSalesforceFields(lastRecord, mapping);
            }
            else{
                log.warn("[UNSUPPORTED_SOURCE] {}", lastRecord.getSource());
                return;
            }

            if(fields == null || fields.isEmpty()){
                log.warn("[FIELDS_EMPTY] No fields created");
                return;
            }

            OverlapRecordsRequest request =
                    buildRequestFromLastRecord(lastRecord);

            request.setFields(fields);

            saveByRecordType(request);

            log.info("[VERSION_CREATED] orgId={} newVersionTriggered",
                    lastRecord.getOrganizationId());

        }catch(Exception e){

            log.error("[VERSION_CREATION_ERROR] orgId={}",
                    lastRecord.getOrganizationId(), e);
        }
    }

    /**
     * BUILD REQUEST BASED ON PREVIOUS RECORD
     */
    private OverlapRecordsRequest buildRequestFromLastRecord(
            OverlapRecordEntity record){

        OverlapRecordsRequest request = new OverlapRecordsRequest();

        request.setOrganizationId(record.getOrganizationId());
        request.setRecordType(record.getRecordType());

        request.setFileName("hubspot_auto_sync.csv");

        request.setSource(record.getSource());
        request.setFrequency(record.getFrequency());

        request.setGoogleSheetLink("https://static.sheet/test");

        request.setFieldToColumnMapping(record.getFieldToColumnMapping());

        request.setUserId("SYSTEM");

        return request;
    }

    /**
     * CHECK IF NEW VERSION SHOULD BE CREATED
     */
    private boolean shouldCreateNextVersion(OverlapRecordEntity record){

        if(record.getFrequency() == null){
            return false;
        }

        Duration duration = record.getFrequency().getDuration();

        if(duration == null || duration.isZero()){
            return false;
        }

        if(record.getCreationTimestamp() == null){
            return false;
        }

        Instant created = record.getCreationTimestamp().toInstant();
        Instant nextRun = created.plus(duration);

        Instant now = Instant.now();

        if(now.isBefore(nextRun)){
            return false;
        }

        log.info("[FREQUENCY_MATCHED] orgId={} frequency={}",
                record.getOrganizationId(),
                record.getFrequency());

        return true;
    }

    /**
     * BUILD HUBSPOT PROPERTY STRING
     */
    public String buildHubspotProperties(Map<String,String> mapping){

        return mapping.values()
                .stream()
                .filter(Objects::nonNull)
                .filter(v -> !v.isBlank())
                .collect(Collectors.joining(","));
    }

    /**
     * CONVERT HUBSPOT RESPONSE TO OVERLAP FIELDS
     */
    private List<OverlapRecordsField> buildFieldsFromHubspot(
            Map<Object,Object> hubspotResponse,
            Map<String,String> mapping){

        Object resultsObj = hubspotResponse.get("results");

        if(!(resultsObj instanceof List<?> results)){
            log.warn("[HUBSPOT_STRUCTURE_INVALID]");
            return Collections.emptyList();
        }

        List<OverlapRecordsField> fields = new ArrayList<>();

        for(Object obj : results){

            if(!(obj instanceof Map<?,?> recordMap)){
                continue;
            }

            Object propertiesObj = recordMap.get("properties");

            if(!(propertiesObj instanceof Map<?,?> properties)){
                continue;
            }

            OverlapRecordsField field = new OverlapRecordsField();

            field.setDomain(
                    safeValue(properties, mapping.get("domain"))
            );

            field.setCompanyName(
                    safeValue(properties, mapping.get("companyName"))
            );

            field.setContactEmail(
                    safeValue(properties, mapping.get("contactEmail"))
            );

            field.setCreationDate(
                    safeValue(properties, mapping.get("creationDate"))
            );

            field.setCloseDate(
                    safeValue(properties, mapping.get("closeDate"))
            );

            field.setSubscribed(
                    safeValue(properties, mapping.get("subscribed"))
            );

            fields.add(field);
        }

        log.info("[FIELDS_CREATED] count={}", fields.size());

        return fields;
    }

    /**
     * SAFE VALUE FETCH
     */
    private String safeValue(Map<?,?> map, String key){

        if(key == null){
            return null;
        }

        Object value = map.get(key);

        return value != null ? value.toString() : null;
    }

    /**
     * CONVERT OLD ENTITY FIELD
     */
    private OverlapRecordsField convertField(
            OverlapRecordFieldEntity entity){

        if(entity == null){
            return null;
        }

        OverlapRecordsField field = new OverlapRecordsField();

        field.setName(entity.getName());
        field.setCompanyName(entity.getCompanyName());
        field.setContactEmail(entity.getContactEmail());
        field.setDomain(entity.getDomain());
        field.setDealStage(entity.getDealStage());
        field.setCreationDate(entity.getCreationDate());
        field.setCloseDate(entity.getCloseDate());
        field.setSubscribed(entity.getSubscribed());
        field.setTicketSize(entity.getTicketSize());

        return field;
    }


    public void triggerManualVersionFromToken() {

        Long orgId = Util.getOrgIdFromToken();

        log.info("[MANUAL_VERSION_TRIGGER] orgId={}", orgId);

        Optional<OverlapRecordEntity> lastRecordOpt =
                overlapRecordsRepository
                        .findTopByOrganizationIdOrderByVersionDesc(orgId);


        if (lastRecordOpt.isEmpty()) {
            log.warn("[MANUAL_VERSION_TRIGGER] No overlap record found orgId={}", orgId);
            return;
        }

        OverlapRecordEntity lastRecord = lastRecordOpt.get();

        try {

            Map<String, String> mapping = lastRecord.getFieldToColumnMapping();

            if (mapping == null || mapping.isEmpty()) {
                log.warn("[MANUAL_VERSION_TRIGGER] Mapping empty orgId={}", orgId);
                return;
            }

            String properties = buildHubspotProperties(mapping);

            log.info("[MANUAL_HUBSPOT_CALL] properties={}", properties);

            Map<Object, Object> hubspotData =
                    hubspotService.getDetails(orgId, properties);

            if (hubspotData == null || hubspotData.isEmpty()) {
                log.warn("[MANUAL_VERSION_TRIGGER] HubSpot returned empty response");
                return;
            }

            List<OverlapRecordsField> fields =
                    buildFieldsFromHubspot(hubspotData, mapping);

            if (fields.isEmpty()) {
                log.warn("[MANUAL_VERSION_TRIGGER] No fields generated from hubspot");
                return;
            }

            OverlapRecordsRequest request =
                    buildRequestFromLastRecord(lastRecord);

            request.setFields(fields);

            personaService.saveOverlapRecords(request);

            log.info("[MANUAL_VERSION_CREATED] orgId={}", orgId);

        } catch (Exception e) {
            log.error("[MANUAL_VERSION_ERROR] orgId={}", orgId, e);
        }
    }

    private List<OverlapRecordsField> fetchSalesforceFields(
            OverlapRecordEntity record,
            Map<String,String> mapping){

        try{

            List<String> fields =
                    mapping.values()
                            .stream()
                            .filter(Objects::nonNull)
                            .toList();

            Map<Object,Object> response =
                    salesforceService.getDetails(
                            record.getOrganizationId(),
                            fields
                    );

            if(response == null || response.isEmpty()){
                log.warn("[SALESFORCE_EMPTY_RESPONSE]");
                return Collections.emptyList();
            }

            return buildFieldsFromSalesforce(response, mapping);

        }catch(Exception ex){

            log.error("[SALESFORCE_FETCH_ERROR]", ex);
            return Collections.emptyList();
        }
    }

    private List<OverlapRecordsField> buildFieldsFromSalesforce(
            Map<Object,Object> response,
            Map<String,String> mapping){

        Object recordsObj = response.get("records");

        if(!(recordsObj instanceof List<?> records)){
            log.warn("[SALESFORCE_STRUCTURE_INVALID]");
            return Collections.emptyList();
        }

        List<OverlapRecordsField> fields = new ArrayList<>();

        for(Object obj : records){

            if(!(obj instanceof Map<?,?> record)){
                continue;
            }

            OverlapRecordsField field = new OverlapRecordsField();

            field.setDomain(
                    safeValue(record, mapping.get("domain"))
            );

            field.setCompanyName(
                    safeValue(record, mapping.get("companyName"))
            );

            field.setContactEmail(
                    safeValue(record, mapping.get("contactEmail"))
            );

            field.setCreationDate(
                    safeValue(record, mapping.get("creationDate"))
            );

            field.setCloseDate(
                    safeValue(record, mapping.get("closeDate"))
            );

            field.setSubscribed(
                    safeValue(record, mapping.get("subscribed"))
            );

            fields.add(field);
        }

        log.info("[SALESFORCE_FIELDS_CREATED] count={}", fields.size());

        return fields;
    }

    public void saveByRecordType(OverlapRecordsRequest request) {

        switch (request.getRecordType()) {

            case CUSTOMER -> customerPersonaService.saveOverlapRecordsForCustomerVersion(request);

            case PROSPECT -> prospectPersonaService.saveOverlapRecordsForProspect(request);

            case OPPORTUNITY -> opportunityPersonaService.saveOverlapRecordsForOpportunity(request);

            default -> throw new RuntimeException("Unsupported record type");
        }
    }
}
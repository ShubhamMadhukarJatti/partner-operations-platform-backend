package com.sharkdom.datapipeline.service;

import com.sharkdom.entity.ai.OverlapRecordEntity;
import com.sharkdom.entity.ai.OverlapRecordFieldEntity;
import com.sharkdom.entity.ai.PersonaStatusEntity;
import com.sharkdom.model.PersonaStatus;
import com.sharkdom.model.ai.Frequency;
import com.sharkdom.model.ai.OverlapRecordsRequest;
import com.sharkdom.model.ai.PersonaRequest;
import com.sharkdom.repository.ai.OverlapRecordsRepository;
import com.sharkdom.repository.ai.PersonaStatusRepository;
import com.sharkdom.service.ai.AsyncPersonaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service responsible for handling Overlap Records ingestion and triggering Persona processing.
 *
 * <p>
 * This service:
 * <ul>
 *     <li>Stores overlap records (Opportunity / Customer)</li>
 *     <li>Maintains versioning</li>
 *     <li>Triggers persona generation pipeline</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
public class DataPipeLineService {

    @Autowired
    private OverlapRecordsRepository overlapRecordsRepository;

    @Autowired
    private PersonaStatusRepository personaStatusRepository;

    @Autowired
    private AsyncPersonaService asyncPersonaService;

    /**
     * Saves overlap records for Opportunity type and initializes persona processing.
     *
     * @param request overlap records request payload
     * @return persisted {@link OverlapRecordEntity}
     */
    @Transactional
    public OverlapRecordEntity saveOverlapRecordsForOpportunity(OverlapRecordsRequest request) {

        Long orgId = request.getOrganizationId();
        log.info("Starting Opportunity Overlap processing for orgId={}", orgId);

        OverlapRecordEntity lastRecord =
                overlapRecordsRepository
                        .findTopByOrganizationIdAndRecordTypeOrderByVersionDesc(
                                orgId, request.getRecordType())
                        .orElse(null);

        Integer version = (lastRecord != null) ? lastRecord.getVersion() + 1 : 1;
        Integer versionId = generateNextVersionId(orgId);

        log.info("Computed version={} and versionId={} for orgId={}", version, versionId, orgId);

        OverlapRecordEntity overlapRecordEntity = new OverlapRecordEntity();
        overlapRecordEntity.setOrganizationId(orgId);
        overlapRecordEntity.setRecordType(request.getRecordType());
        overlapRecordEntity.setFileName(request.getFileName());
        overlapRecordEntity.setSource(request.getSource());
        overlapRecordEntity.setFrequency(request.getFrequency());
        overlapRecordEntity.setGoogleSheetLink(request.getGoogleSheetLink());
        overlapRecordEntity.setFieldToColumnMapping(request.getFieldToColumnMapping());
        overlapRecordEntity.setVersion(version);
        overlapRecordEntity.setVersionId(versionId);

        List<String> websites = new ArrayList<>();

        if (request.getFields() != null && !request.getFields().isEmpty()) {

            log.info("Processing {} opportunity fields for orgId={}", request.getFields().size(), orgId);

            List<OverlapRecordFieldEntity> fields = request.getFields()
                    .stream()
                    .map(fieldRequest -> {

                        OverlapRecordFieldEntity field = new OverlapRecordFieldEntity();
                        field.setDealName(fieldRequest.getDealName());
                        field.setDealOwner(fieldRequest.getDealOwner());
                        field.setAmountAcv(fieldRequest.getAmountAcv());
                        field.setDealId(fieldRequest.getDealId());
                        field.setPipeline(fieldRequest.getPipeline());
                        field.setDealType(fieldRequest.getDealType());
                        field.setAssociatedContactId(fieldRequest.getAssociatedContactId());

                        field.setVersion(version);
                        field.setVersionId(versionId);
                        field.setOverlapRecord(overlapRecordEntity);

                        websites.add(fieldRequest.getDomain());

                        return field;
                    }).toList();

            overlapRecordEntity.getFields().addAll(fields);
        }

        var freq = Frequency.valueOf(request.getFrequency().name());

        PersonaRequest personaRequest = new PersonaRequest(
                orgId,
                websites,
                new String[]{},
                freq,
                "",
                request.getFieldToColumnMapping().get("domain"),
                request.getGoogleSheetLink(),
                request.getSource(),
                request.getFileName()
        );

        log.info("Creating PersonaStatus entry for orgId={} versionId={}", orgId, versionId);

        PersonaStatusEntity status = new PersonaStatusEntity();
        status.setOrganizationId(orgId);
        status.setVersion(version);
        status.setVersionId(versionId);
        status.setColumnName(personaRequest.getColumnName());
        status.setGoogleSheetLink(personaRequest.getGoogleSheetLink());
        status.setPersonaMode(personaRequest.getPersonaMode());
        status.setFileName(personaRequest.getFileName());
        status.setFrequency(personaRequest.getFrequency());
        status.setPersonaStatus(PersonaStatus.INITIATED);

        personaStatusRepository.save(status);

        OverlapRecordEntity savedRecord = overlapRecordsRepository.save(overlapRecordEntity);

        log.info("Completed Opportunity Overlap processing for orgId={} versionId={}", orgId, versionId);

        return savedRecord;
    }

    /**
     * Saves overlap records for Customer type and triggers async persona processing.
     *
     * @param request overlap records request payload
     * @return persisted {@link OverlapRecordEntity}
     */
    @Transactional
    public OverlapRecordEntity saveOverlapRecordsForCustomer(OverlapRecordsRequest request) {

        Long orgId = request.getOrganizationId();
        log.info("Starting Customer Overlap processing for orgId={}", orgId);

        OverlapRecordEntity lastRecord =
                overlapRecordsRepository
                        .findTopByOrganizationIdAndRecordTypeOrderByVersionDesc(
                                orgId, request.getRecordType())
                        .orElse(null);

        Integer version = (lastRecord != null) ? lastRecord.getVersion() + 1 : 1;
        Integer versionId = generateNextVersionId(orgId);

        log.info("Computed version={} and versionId={} for orgId={}", version, versionId, orgId);

        OverlapRecordEntity overlapRecordEntity = new OverlapRecordEntity();
        overlapRecordEntity.setOrganizationId(orgId);
        overlapRecordEntity.setRecordType(request.getRecordType());
        overlapRecordEntity.setFileName(request.getFileName());
        overlapRecordEntity.setSource(request.getSource());
        overlapRecordEntity.setFrequency(request.getFrequency());
        overlapRecordEntity.setGoogleSheetLink(request.getGoogleSheetLink());
        overlapRecordEntity.setFieldToColumnMapping(request.getFieldToColumnMapping());
        overlapRecordEntity.setVersion(version);
        overlapRecordEntity.setVersionId(versionId);

        List<String> websites = new ArrayList<>();

        if (request.getFields() != null && !request.getFields().isEmpty()) {

            log.info("Processing {} customer fields for orgId={}", request.getFields().size(), orgId);

            List<OverlapRecordFieldEntity> fields = request.getFields()
                    .stream()
                    .map(fieldRequest -> {

                        OverlapRecordFieldEntity field = new OverlapRecordFieldEntity();
                        field.setWebsite(fieldRequest.getWebsite());
                        field.setCompanyName(fieldRequest.getCompanyName());
                        field.setIndustry(fieldRequest.getIndustry());
                        field.setCompanySize(fieldRequest.getCompanySize());
                        field.setCountry(fieldRequest.getCountry());
                        field.setLinkedinUrl(fieldRequest.getLinkedinUrl());
                        field.setAnnualRevenue(fieldRequest.getAnnualRevenue());
                        field.setDescription(fieldRequest.getDescription());
                        field.setCompanyPhone(fieldRequest.getCompanyPhone());
                        field.setCity(fieldRequest.getCity());

                        field.setVersion(version);
                        field.setVersionId(versionId);
                        field.setOverlapRecord(overlapRecordEntity);

                        websites.add(fieldRequest.getWebsite());

                        return field;
                    }).toList();

            overlapRecordEntity.getFields().addAll(fields);
        }

        var freq = Frequency.valueOf(request.getFrequency().name());

        PersonaRequest personaRequest = new PersonaRequest(
                orgId,
                websites,
                new String[]{},
                freq,
                "",
                request.getFieldToColumnMapping().get("domain"),
                request.getGoogleSheetLink(),
                request.getSource(),
                request.getFileName()
        );

        log.info("Creating PersonaStatus and triggering async persona for orgId={} versionId={}", orgId, versionId);

        PersonaStatusEntity status = new PersonaStatusEntity();
        status.setOrganizationId(orgId);
        status.setVersion(version);
        status.setVersionId(versionId);
        status.setColumnName(personaRequest.getColumnName());
        status.setGoogleSheetLink(personaRequest.getGoogleSheetLink());
        status.setPersonaMode(personaRequest.getPersonaMode());
        status.setFileName(personaRequest.getFileName());
        status.setFrequency(personaRequest.getFrequency());
        status.setPersonaStatus(PersonaStatus.INITIATED);

        personaStatusRepository.save(status);

        asyncPersonaService.savePersona(personaRequest, version, versionId);

        OverlapRecordEntity savedRecord = overlapRecordsRepository.save(overlapRecordEntity);

        log.info("Completed Customer Overlap processing for orgId={} versionId={}", orgId, versionId);

        return savedRecord;
    }

    /**
     * Saves overlap records for Prospect type.
     *
     * @param request overlap records request payload
     * @return persisted {@link OverlapRecordEntity}
     */
    @Transactional
    public OverlapRecordEntity saveOverlapRecordsForProspect(OverlapRecordsRequest request) {

        Long orgId = request.getOrganizationId();

        OverlapRecordEntity lastRecord =
                overlapRecordsRepository
                        .findTopByOrganizationIdAndRecordTypeOrderByVersionDesc(
                                orgId, request.getRecordType())
                        .orElse(null);

        Integer version = (lastRecord != null) ? lastRecord.getVersion() + 1 : 1;

        Integer versionId = generateNextVersionId(orgId);

        log.info("Processing overlap orgId={} version={} versionId={}",
                orgId, version, versionId);

        // ALWAYS CREATE NEW ENTITY (NO UPDATE)
        OverlapRecordEntity overlapRecordEntity = new OverlapRecordEntity();

        overlapRecordEntity.setOrganizationId(orgId);
        overlapRecordEntity.setRecordType(request.getRecordType());
        overlapRecordEntity.setFileName(request.getFileName());
        overlapRecordEntity.setSource(request.getSource());
        overlapRecordEntity.setFrequency(request.getFrequency());
        overlapRecordEntity.setGoogleSheetLink(request.getGoogleSheetLink());
        overlapRecordEntity.setFieldToColumnMapping(request.getFieldToColumnMapping());

        overlapRecordEntity.setVersion(version);
        overlapRecordEntity.setVersionId(versionId);

        List<String> websites = new ArrayList<>();

        if (request.getFields() != null && !request.getFields().isEmpty()) {

            List<OverlapRecordFieldEntity> fields = request.getFields()
                    .stream()
                    .map(fieldRequest -> {

                        OverlapRecordFieldEntity field = new OverlapRecordFieldEntity();

                        field.setName(fieldRequest.getName());
                        field.setCompanyName(fieldRequest.getCompanyName());
                        field.setContactEmail(fieldRequest.getContactEmail());
                        field.setDomain(fieldRequest.getDomain());
                        field.setDealStage(fieldRequest.getDealStage());
                        field.setCreationDate(fieldRequest.getCreationDate());
                        field.setCloseDate(fieldRequest.getCloseDate());
                        field.setSubscribed(fieldRequest.getSubscribed());
                        field.setTicketSize(fieldRequest.getTicketSize());
                        field.setFirstName(fieldRequest.getFirstName());
                        field.setLastName(fieldRequest.getLastName());
                        field.setJobTitle(fieldRequest.getJobTitle());
                        field.setContactLinkedinUrl(fieldRequest.getContactLinkedinUrl());
                        field.setLeadStatus(fieldRequest.getLeadStatus());
                        field.setContactPhone(fieldRequest.getContactPhone());
                        field.setLastActivityDate(fieldRequest.getLastActivityDate());
                        field.setContactOwner(fieldRequest.getContactOwner());
                        field.setAssociatedCompanyId(fieldRequest.getAssociatedCompanyId());

                        field.setVersion(version);
                        field.setVersionId(versionId);

                        field.setOverlapRecord(overlapRecordEntity);

                        websites.add(fieldRequest.getDomain());

                        return field;

                    }).toList();

            overlapRecordEntity.getFields().addAll(fields);
        }

        var freq = Frequency.valueOf(request.getFrequency().name());

        PersonaRequest personaRequest = new PersonaRequest(
                orgId,
                websites,
                new String[]{},
                freq,
                "",
                request.getFieldToColumnMapping().get("domain"),
                request.getGoogleSheetLink(),
                request.getSource(),
                request.getFileName()
        );

        PersonaStatusEntity status = new PersonaStatusEntity();

        status.setOrganizationId(orgId);
        status.setVersion(version);
        status.setVersionId(versionId);
        status.setColumnName(personaRequest.getColumnName());
        status.setGoogleSheetLink(personaRequest.getGoogleSheetLink());
        status.setPersonaMode(personaRequest.getPersonaMode());
        status.setFileName(personaRequest.getFileName());
        status.setFrequency(personaRequest.getFrequency());
        status.setPersonaStatus(PersonaStatus.INITIATED);
        personaStatusRepository.save(status);
        return overlapRecordsRepository.save(overlapRecordEntity);
    }

    /**
     * Generates next versionId using last stored versionId + random increment.
     *
     * @param orgId organization id
     * @return next versionId
     */
    private Integer generateNextVersionId(Long orgId) {

        PersonaStatusEntity lastVersion =
                personaStatusRepository
                        .findTopByOrganizationIdOrderByVersionIdDesc(orgId);

        if (lastVersion == null || lastVersion.getVersionId() == null) {
            log.warn("No previous versionId found for orgId={}, starting from default", orgId);
            return 11111;
        }

        int base = lastVersion.getVersionId();
        int increment = new Random().nextInt(37) + 9;

        int nextVersionId = base + increment;

        log.debug("Generated versionId={} from base={} with increment={} for orgId={}",
                nextVersionId, base, increment, orgId);

        return nextVersionId;
    }
}
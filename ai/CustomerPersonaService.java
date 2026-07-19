package com.sharkdom.service.ai;

import com.sharkdom.entity.ai.OverlapRecordEntity;
import com.sharkdom.entity.ai.OverlapRecordFieldEntity;
import com.sharkdom.entity.ai.PersonaStatusEntity;
import com.sharkdom.model.PersonaStatus;
import com.sharkdom.model.ai.Frequency;
import com.sharkdom.model.ai.OverlapRecordsRequest;
import com.sharkdom.model.ai.PersonaRequest;
import com.sharkdom.repository.ai.OverlapRecordsRepository;
import com.sharkdom.repository.ai.PersonaStatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class CustomerPersonaService {

    @Autowired
    private OverlapRecordsRepository overlapRecordsRepository;

    @Autowired
    private PersonaStatusRepository personaStatusRepository;

    @Autowired
    private AsyncPersonaService asyncPersonaService;


    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate9DigitAlphaNumeric() {
        StringBuilder builder = new StringBuilder(9);

        for (int i = 0; i < 9; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            builder.append(CHARACTERS.charAt(index));
        }

        return builder.toString();
    }

    @Transactional
    public OverlapRecordEntity saveOverlapRecordsForCustomer(OverlapRecordsRequest request) {

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
        return savedRecord;
    }

    private Integer generateNextVersionId(Long orgId) {

        PersonaStatusEntity lastVersion =
                personaStatusRepository
                        .findTopByOrganizationIdOrderByVersionIdDesc(orgId);

        int base;

        if (lastVersion == null || lastVersion.getVersionId() == null) {
            return 11111;
        }

        base = lastVersion.getVersionId();

        int increment = new Random().nextInt(37) + 9;

        return base + increment;
    }


    @Transactional
    public OverlapRecordEntity saveOverlapRecordsForCustomerVersion(OverlapRecordsRequest request) {

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
        return savedRecord;
    }

}

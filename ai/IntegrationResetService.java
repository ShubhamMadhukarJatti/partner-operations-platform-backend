package com.sharkdom.service.ai;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.entity.ai.OverlapRecordEntity;
import com.sharkdom.entity.ai.PersonaStatusEntity;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.model.PersonaStatus;
import com.sharkdom.model.ai.OverlapFrequency;
import com.sharkdom.model.ai.RecordType;
import com.sharkdom.repository.ai.OverlapRecordsRepository;
import com.sharkdom.repository.ai.PersonaStatusRepository;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationResetService {

    private final IntegrationRepository integrationRepository;
    private final OverlapRecordsRepository overlapRecordRepository;
    private final PersonaStatusRepository personaStatusRepository;

    public void resetIntegrationAndFrequency(IntegrationType integrationType, RecordType recordType) {

        Long orgId = Util.getOrgIdFromToken();

        log.info("Reset request received for orgId={}, integrationType={}", orgId, integrationType);

        // Step 1: Reset Refresh Token
        IntegrationDetails integration = integrationRepository
                .findByOrganizationIdAndIntegrationType(orgId, integrationType);

        if (integration == null) {
            log.error("Integration not found for orgId={} and type={}", orgId, integrationType);
            throw new ServiceException(ErrorMessages.NOT_FOUND);
        }

        integration.setRefreshToken(null);
        integration.setConnected(false);

        integrationRepository.save(integration);

        log.info("Refresh token cleared for integrationType={}", integrationType);

        // Step 2: Reset Frequency (FIXED METHOD)
        OverlapRecordEntity record =
                overlapRecordRepository
                        .findTopByOrganizationIdAndRecordTypeOrderByVersionDesc(orgId,recordType)
                        .orElseThrow(() -> {
                            log.error("Overlap record not found for orgId={}", orgId);
                            return new ServiceException(ErrorMessages.NOT_FOUND);
                        });

        record.setFrequency(OverlapFrequency.NONE);
        overlapRecordRepository.save(record);
        var statusEntity = personaStatusRepository.getByOrganizationId(orgId);
        if (statusEntity!=null)
        {
            statusEntity.setPersonaStatus(PersonaStatus.NONE);
            personaStatusRepository.save(statusEntity);
        }

        log.info("Frequency reset to NONE for recordId={}", record.getId());
    }
}
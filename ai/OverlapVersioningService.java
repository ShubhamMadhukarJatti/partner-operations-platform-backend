package com.sharkdom.service.ai;

import com.sharkdom.model.ai.OverlapCronExecutionEntity;
import com.sharkdom.entity.ai.OverlapRecordEntity;
import com.sharkdom.entity.ai.OverlapRecordFieldEntity;
import com.sharkdom.model.ai.CronStatus;
import com.sharkdom.model.ai.OverlapRequest;
import com.sharkdom.model.ai.RecordType;
import com.sharkdom.repository.ai.OverlapCronExecutionRepository;
import com.sharkdom.repository.ai.OverlapRecordsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OverlapVersioningService {

    private final OverlapRecordsRepository recordRepo;
    private final OverlapCronExecutionRepository cronRepo;

    @Transactional
    public void runVersioning(OverlapRequest request) {

        if (request == null || request.getFields() == null || request.getFields().isEmpty()) {
            log.warn("⚠ OverlapRequest invalid or empty. Skipping versioning. orgId={}",
                    request != null ? request.getOrganizationId() : "UNKNOWN");
            return;
        }

        log.info("📦 Starting versioning for orgId={} persona={} totalFields={}",
                request.getOrganizationId(),
                request.getPersona(),
                request.getFields().size());

        OverlapCronExecutionEntity cron = createCron(request);

        try {

            Integer newVersion = getNextVersion(request);

            log.info("🆕 Creating new version={} for orgId={} persona={}",
                    newVersion,
                    request.getOrganizationId(),
                    request.getPersona());

            OverlapRecordEntity record =
                    createParentRecord(request, newVersion);

            List<OverlapRecordFieldEntity> fieldEntities =
                    convertFields(request, record);

            log.debug("📝 Converted {} fields for version={}",
                    fieldEntities.size(),
                    newVersion);

            record.setFields(fieldEntities);

            recordRepo.save(record);

            log.info("✅ Record saved successfully. orgId={} version={}",
                    request.getOrganizationId(),
                    newVersion);

            cron.setStatus(CronStatus.SUCCESS);
            cron.setVersionCreated(newVersion);
            cron.setCompletedTime(Instant.now());

        } catch (Exception ex) {

            log.error("❌ Versioning failed for orgId={} persona={}",
                    request.getOrganizationId(),
                    request.getPersona(),
                    ex);

            cron.setStatus(CronStatus.FAILED);
            cron.setErrorMessage(ex.getMessage());
            cron.setCompletedTime(Instant.now());
        }

        cronRepo.save(cron);

        log.info("📊 Cron execution updated with status={} for orgId={}",
                cron.getStatus(),
                request.getOrganizationId());
    }

    private OverlapCronExecutionEntity createCron(
            OverlapRequest request) {

        OverlapCronExecutionEntity cron =
                new OverlapCronExecutionEntity();

        cron.setOrganizationId(request.getOrganizationId());
        cron.setPersona(request.getPersona());
        cron.setExecutionTime(Instant.now());
        cron.setStatus(CronStatus.RUNNING);

        return cronRepo.save(cron);
    }

    private Integer getNextVersion(
            OverlapRequest request) {

        Integer max =
                recordRepo.findMaxVersion(
                        request.getOrganizationId(),
                        request.getPersona());

        return (max == null) ? 1 : max + 1;
    }

    private OverlapRecordEntity createParentRecord(
            OverlapRequest request,
            Integer version) {

        OverlapRecordEntity record =
                new OverlapRecordEntity();

        record.setOrganizationId(request.getOrganizationId());
        record.setSource(request.getPersona());
        record.setFrequency(request.getFrequency());
        record.setRecordType(request.getRecordType());
        record.setFileName(request.getFileName());
        record.setRecordType(RecordType.CUSTOMER);
        record.setGoogleSheetLink(request.getGoogleSheetLink());
        record.setFieldToColumnMapping(
                request.getFieldToColumnMapping());
        record.setVersion(version);

        return record;
    }

    private List<OverlapRecordFieldEntity> convertFields(
            OverlapRequest request,
            OverlapRecordEntity parent) {

        return request.getFields()
                .stream()
                .map(dto -> {

                    OverlapRecordFieldEntity e =
                            new OverlapRecordFieldEntity();

                    e.setName(dto.getName());
                    e.setCompanyName(dto.getCompanyName());
                    e.setContactEmail(dto.getContactEmail());
                    e.setDomain(dto.getDomain());
                    e.setDealStage(dto.getDealStage());
                    e.setCreationDate(dto.getCreationDate());
                    e.setCloseDate(dto.getCloseDate());
                    e.setSubscribed(dto.getSubscribed());
                    e.setTicketSize(dto.getTicketSize());

                    e.setOverlapRecord(parent);

                    return e;

                }).toList();
    }

}

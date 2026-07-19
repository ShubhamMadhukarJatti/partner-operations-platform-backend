package com.sharkdom.service.ai;

import com.sharkdom.constants.organization.OrganizationStatus;
import com.sharkdom.entity.ai.OverlapRecordEntity;
import com.sharkdom.model.ai.OverlapFrequency;
import com.sharkdom.model.ai.OverlapRequest;
import com.sharkdom.model.ai.PersonaMode;
import com.sharkdom.repository.ai.OverlapRecordsRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverlapScheduler {

    private final OrganizationRepository organizationRepository;
    private final OverlapRecordsRepository overlapRepository;
    private final List<OverlapDataProvider> providers;
    private final OverlapVersioningService versioningService;

    public void runScheduler() {

        long startTime = System.currentTimeMillis();
        log.info("🚀 Overlap Scheduler Started at {}", Instant.now());

        var organizations = organizationRepository.findAll();

        if (organizations.isEmpty()) {
            log.warn("⚠ No organizations found. Scheduler exiting.");
            return;
        }

        log.info("📌 Found {} organizations to process", organizations.size());

        for (var org : organizations) {

            log.info("➡ Processing organizationId={}", org.getId());

            for (var provider : providers) {

                PersonaMode source = provider.getSource();

                try {

                    log.info("🔎 Checking provider={} for orgId={}", source, org.getId());

                    Optional<OverlapRecordEntity> lastRecordOpt =
                            overlapRepository
                                    .findTopByOrganizationIdAndSourceOrderByVersionDesc(
                                            org.getId(),
                                            source
                                    );

                    if (lastRecordOpt.isEmpty()) {

                        log.info("🆕 First time execution. Creating version=1 for orgId={} source={}",
                                org.getId(), source);

                        runNewVersion(org.getId(), provider, null);
                        continue;
                    }

                    OverlapRecordEntity lastRecord = lastRecordOpt.get();

                    log.debug("📄 Last record found: version={}, frequency={}, createdAt={}",
                            lastRecord.getVersion(),
                            lastRecord.getFrequency(),
                            lastRecord.getCreationTimestamp());

                    if (shouldRunNextVersion(lastRecord)) {

                        log.info("⏳ Frequency satisfied. Creating new version for orgId={} source={} previousVersion={}",
                                org.getId(),
                                source,
                                lastRecord.getVersion());

                        runNewVersion(
                                org.getId(),
                                provider,
                                lastRecord.getFrequency()
                        );

                    } else {

                        log.info("⏭ Skipping orgId={} source={} - Next run not due yet",
                                org.getId(),
                                source);
                    }

                } catch (Exception ex) {

                    log.error("❌ Error processing orgId={} provider={}",
                            org.getId(),
                            source,
                            ex);
                }
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("✅ Overlap Scheduler Finished. Total Execution Time: {} ms",
                (endTime - startTime));
    }

    private boolean shouldRunNextVersion(
            OverlapRecordEntity lastRecord) {

        OverlapFrequency frequency =
                lastRecord.getFrequency();

        if (frequency == OverlapFrequency.NONE) {
            log.debug("Frequency NONE. Skipping version creation.");
            return false;
        }

        Instant lastCreatedTime =
                lastRecord.getCreationTimestamp().toInstant();

        Instant nextRunTime =
                lastCreatedTime.plus(
                        frequency.getDuration()
                );

        boolean shouldRun = Instant.now().isAfter(nextRunTime);

        log.debug("LastCreatedTime={}, NextRunTime={}, ShouldRun={}",
                lastCreatedTime,
                nextRunTime,
                shouldRun);

        return shouldRun;
    }

    private void runNewVersion(
            Long orgId,
            OverlapDataProvider provider,
            OverlapFrequency frequency) {

        log.info("Running new version for orgId={} provider={}",
                orgId,
                provider.getSource());

        OverlapRequest request =
                provider.fetch(orgId);

        request.setFrequency(
                frequency != null
                        ? frequency
                        : OverlapFrequency.WEEKLY
        );

        versioningService.runVersioning(request);

        log.info("Versioning completed successfully for orgId={} provider={}",
                orgId,
                provider.getSource());
    }
}
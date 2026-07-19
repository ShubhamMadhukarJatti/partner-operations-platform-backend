package com.sharkdom.onboarding.jobs;

import com.sharkdom.dto.AutomationResponseDto;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.onboarding.service.AutomationDataService;
import com.sharkdom.repository.organization.OrganizationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationEnrichmentScheduler {

    private final TaskScheduler taskScheduler;
    private final OrganizationRepository organizationRepository;
    private final AutomationDataService automationDataService;

    public void scheduleEnrichment(Long orgId) {

        try {
            Instant runAt = Instant.now().plusSeconds(180);

            taskScheduler.schedule(() -> {
                try {
                    log.info("Scheduled enrichment started | orgId={}", orgId);

                    enrichOrganizationData(orgId);

                    log.info("Scheduled enrichment completed | orgId={}", orgId);

                } catch (Exception ex) {
                    log.error("Scheduled enrichment failed | orgId={}", orgId, ex);
                }
            }, Date.from(runAt));

            log.info("Enrichment scheduled after 3 mins | orgId={} | runAt={}", orgId, runAt);

        } catch (Exception e) {
            log.error("Failed to schedule enrichment | orgId={}", orgId, e);
        }
    }

    @Transactional
    public void enrichOrganizationData(Long orgId) {

        var orgOpt = organizationRepository.findById(orgId);

        if (orgOpt.isEmpty()) {
            log.warn("Automation skipped | organization not found | orgId={}", orgId);
            return;
        }

        Organization org = orgOpt.get();

        if (org.getWebsite() == null || org.getWebsite().isBlank()) {
            log.warn("Automation skipped | website empty | orgId={}", orgId);
            return;
        }

        String website = org.getWebsite();

        try {
            log.info("Automation started | orgId={} | website={}", orgId, website);

            AutomationResponseDto response = automationDataService.triggerAutomation(website);

            if (response == null) {
                log.warn("Automation response null | orgId={}", orgId);
                return;
            }

            boolean isUpdated = false;

            // briefDescription mapping
            if (isValid(response.getDescriptionOfStartup()) &&
                    isEmpty(org.getBriefDescription())) {

                org.setBriefDescription(response.getDescriptionOfStartup().trim());
                isUpdated = true;
            }

            // about mapping
            if (isValid(response.getOneLineDescription()) &&
                    isEmpty(org.getAbout())) {

                org.setAbout(response.getOneLineDescription().trim());
                isUpdated = true;
            }

            if (isUpdated) {
                organizationRepository.save(org);
                log.info("Automation success | orgId={} | updated=true", orgId);
            } else {
                log.info("Automation skipped update | orgId={} | no new data", orgId);
            }

        } catch (Exception ex) {
            log.error("Automation failed | orgId={} | website={}", orgId, website, ex);
        }
    }

    // ----------------- UTIL METHODS -----------------

    private boolean isValid(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isEmpty(String value) {
        return value == null || value.isBlank();
    }
}
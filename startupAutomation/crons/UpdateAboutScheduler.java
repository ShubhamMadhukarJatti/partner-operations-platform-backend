package com.sharkdom.startupAutomation.crons;

import com.sharkdom.constants.organization.OrganizationStatus;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.model.organization.OrganizationResponse;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.service.organization.OrganizationService;
import com.sharkdom.startupAutomation.dto.StartupDetailsResponse;
import com.sharkdom.startupAutomation.dto.SubsectorResponse;
import com.sharkdom.startupAutomation.service.AutomationService;
import com.sharkdom.startupAutomation.service.SubsectorService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class UpdateAboutScheduler {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AutomationService automationService;

    @Autowired
    private SubsectorService subsectorService;


    @Transactional
    public void fetchOrganizationsAboutFromWebsite() {
        log.info("Fetching organizations about from website - START");
        try {
            List<Organization> organizations = organizationRepository.findAllByStatusAndFiltersAdded(OrganizationStatus.ACTIVE, false);
            log.info("Total active organizations found: {}", organizations.size());

            for (Organization organization : organizations) {
                try {
                    log.info("Processing organization: {} (ID: {})", organization.getName(), organization.getId());

                    String website = organization.getWebsite();
                    if (website == null || website.isBlank()) {
                        log.warn("Skipping organization {} (ID: {}) - No website found", organization.getName(), organization.getId());
                        continue;
                    }

                    StartupDetailsResponse details = automationService.getStartupDetails(website);
                    if (details == null) {
                        log.warn("No details fetched for organization {} (website: {})", organization.getName(), website);
                        continue;
                    }

                    // Step 1: Set About & Save
                    String descriptionOfTheStartup = details.getDescriptionOfTheStartup();
                    if (descriptionOfTheStartup != null && !descriptionOfTheStartup.isBlank()) {
                        organization.setAbout(descriptionOfTheStartup);
                        Organization savedWithAbout = organizationRepository.save(organization);
                        log.info("About updated for organization {}: {}", savedWithAbout.getName(), descriptionOfTheStartup);

                        // Step 2: Fetch subsectors using updated about
                        SubsectorResponse subsectors = subsectorService.getSubsectors(descriptionOfTheStartup);
                        if (subsectors != null && subsectors.getSubsector() != null && !subsectors.getSubsector().isEmpty()) {
                            savedWithAbout.setFilters(subsectors.getSubsector());
                            savedWithAbout.setFiltersAdded(true);
                            Organization savedWithSubsectors = organizationRepository.save(savedWithAbout);
                            log.info("Subsectors updated for organization {}: {}", savedWithSubsectors.getName(), subsectors.getSubsector());
                        } else {
                            log.warn("No subsectors found for organization {}", savedWithAbout.getName());
                        }
                    } else {
                        log.warn("No about description found for organization {}", organization.getName());
                    }

                } catch (Exception ex) {
                    log.error("Failed to process organization {} (ID: {}) - {}", organization.getName(), organization.getId(), ex.getMessage(), ex);
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch organizations about from website: {}", e.getMessage(), e);
        }
        log.info("Fetching organizations about from website - END");
    }
}

//    @EventListener(ApplicationReadyEvent.class)
//    public void onApplicationReady() {
//        fetchOrganizationsAboutFromWebsite();
//    }

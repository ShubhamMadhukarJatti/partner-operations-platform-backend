package com.sharkdom.service.organization;

import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.model.email.EmailReqModelWithMultipartAttachments;
import com.sharkdom.model.email.EmailTemplate;
import com.sharkdom.repository.ai.PersonaNotifyRepository;
import com.sharkdom.repository.ai.PersonaRepository;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organization.VisitorOrganizationRepository;
import com.sharkdom.repository.organizationcollaboration.OrganizationCollaborationRepository;
import com.sharkdom.repository.organizationcollaboration.OrganizationMessagesRepository;
import com.sharkdom.repository.organizationcollaboration.PartnershipMouVersionRepository;
import com.sharkdom.service.email.EmailFromTemplateService;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.util.Util;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class WeeklyOutcomeScheduler {
    @Value("${env}")
    private String configEnv;
    private final VisitorOrganizationRepository visitorOrganizationRepository;
    private final OrganizationMessagesRepository organizationMessagesRepository;
    private final PersonaNotifyRepository personaNotifyRepository;
    private final IntegrationRepository integrationRepository;
    private final PersonaRepository personaRepository;
    private final OrganizationRepository organizationRepository;
    private final EmailService emailService;
    private final EmailFromTemplateService emailFromTemplateService;
    private final OrganizationCollaborationRepository organizationCollaborationRepository;
    private final PartnershipMouVersionRepository partnershipMouVersionRepository;
    private static final String PROGRESS_FRACTION = "<java>{{progressFraction}}</java>";
    private static final String PROGRESS_PERCENTAGE = "<java>{{progressPercentage}}</java>";
    private static final String ORG_NAME = "<java>{{orgName}}</java>";
    private static final String TASKS_LIST = "<java>{{tasksList}}</java>";
    private static final String PERSONA_REQUESTED = "<java>{{personaRequested}}</java>";
    private static final String NEW_PARTNERSHIPS = "<java>{{newPartnerships}}</java>";
    private static final String PARTNERSHIP_INITIATED = "<java>{{partnershipInitiated}}</java>";
    private static final String IPP_RATE = "<java>{{ippRate}}</java>";
    private static final String TOTAL_ENQUIRIES = "<java>{{totalEnquiries}}</java>";
    private static final String PARTNERS_COMMUNICATED = "<java>{{partnersCommunicated}}</java>";

    public WeeklyOutcomeScheduler(VisitorOrganizationRepository visitorOrganizationRepository, OrganizationMessagesRepository organizationMessagesRepository, PersonaNotifyRepository personaNotifyRepository, IntegrationRepository integrationRepository, PersonaRepository personaRepository, OrganizationRepository organizationRepository, EmailService emailService, EmailFromTemplateService emailFromTemplateService, OrganizationCollaborationRepository organizationCollaborationRepository, PartnershipMouVersionRepository partnershipMouVersionRepository) {
        this.visitorOrganizationRepository = visitorOrganizationRepository;
        this.organizationMessagesRepository = organizationMessagesRepository;
        this.personaNotifyRepository = personaNotifyRepository;
        this.integrationRepository = integrationRepository;
        this.personaRepository = personaRepository;
        this.organizationRepository = organizationRepository;
        this.emailService = emailService;
        this.emailFromTemplateService = emailFromTemplateService;
        this.organizationCollaborationRepository = organizationCollaborationRepository;
        this.partnershipMouVersionRepository = partnershipMouVersionRepository;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    public static class TemplateData {
        private String templateCode;
        private String orgName;
        private List<Task> tasks;
        private Progress progress;
        private Summary summary;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Progress {
        private int completed;
        private int total;

        public int getPercentage() {
            return (completed * 100) / total;
        }

        public String getFraction() {
            return completed + "/" + total;
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Summary {
        private String personaRequested;
        private String newPartnerships;
        private String partnershipInitiated;
        private String ippRate;
        private String totalEnquiries;
        private String partnersCommunicated;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Task {
        private String name;
        private boolean completed;
    }

    @Scheduled(cron = "0 45 09 ? * TUE", zone = "Asia/Kolkata")
    public void runWeeklyTuesdayTask() {
        log.info("Weekly Tuesday Scheduler triggered");
        if (!configEnv.equalsIgnoreCase("dev")) {
            log.info("Environment is {}, sending emails", configEnv);
            sendEmails();
        } else {
            log.warn("Running in DEV environment - emails will NOT be sent");
        }
    }

    @Scheduled(cron = "0 45 14 ? * FRI", zone = "Asia/Kolkata")
    public void runWeeklyFridayTask() {
        log.info("Weekly Friday Scheduler triggered");
        if (!configEnv.equalsIgnoreCase("dev")) {
            log.info("Environment is {}, sending emails", configEnv);
            sendEmails();
        } else {
            log.warn("Running in DEV environment - emails will NOT be sent");
        }
    }

    public void sendEmails() {
        long startTime = System.currentTimeMillis();
        log.info("Weekly Outcome Email job started");

        try {
            List<Organization> organizations = organizationRepository.getAllOrganizations();
            log.info("Total organizations fetched: {}", organizations.size());

            organizations.forEach(organization -> {

                Long orgId = organization.getId();
                String orgName = organization.getName();
                log.info("=======================================");
                log.info("Processing Organization | ID: {} | Name: {}", orgId, orgName);

                try {
                    var personaCreated = !personaRepository.getAllByOrganizationId(orgId).isEmpty();
                    var meetModesSet = integrationRepository
                            .existsByOrganizationIdAndIntegrationTypeAndIsConnectedTrue(orgId, IntegrationType.G_CALENDAR);
                    var profileCompleted = Util.getOrganizationProgress(organization) == 100;

                    log.info("Tasks status | Persona: {} | G-Calendar: {} | Profile: {}",
                            personaCreated, meetModesSet, profileCompleted);

                    int completedCount = 0;
                    if (personaCreated) completedCount++;
                    if (meetModesSet) completedCount++;
                    if (profileCompleted) completedCount++;

                    List<Task> tasks = Arrays.asList(
                            Task.builder().name("Create your customer persona").completed(personaCreated).build(),
                            Task.builder().name("Set Preferred Meet Modes").completed(meetModesSet).build(),
                            Task.builder().name("Complete business profile").completed(profileCompleted).build()
                    );

                    Progress progress = Progress.builder()
                            .completed(completedCount)
                            .total(3)
                            .build();

                    log.info("Progress for org {} → {}/{} ({}%)",
                            orgName, progress.getCompleted(), progress.getTotal(), progress.getPercentage());

                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime lastWeek = now.minusDays(7);

                    var personaRequested = personaNotifyRepository.countBySenderIdInLastWeek(orgId, lastWeek, now);
                    var partnershipInitiated = organizationCollaborationRepository.countBySenderIdInWeek(orgId, lastWeek, now);
                    var enquiries = organizationCollaborationRepository.countByReceiverIdInWeek(orgId, lastWeek, now);
                    var collaborationsIds = organizationCollaborationRepository.getAllCollaborationsInWeek(orgId, lastWeek, now);
                    var newPartnerShips = partnershipMouVersionRepository
                            .getAllActiveCollaborationsInWeek(collaborationsIds, lastWeek, now);
                    var communicated = organizationMessagesRepository
                            .countMessageInLastWeek(orgId, collaborationsIds, lastWeek, now);
                    var visitorOrganizations = visitorOrganizationRepository.visitorsInLastWeek(orgId, lastWeek, now);
                    var ippRate = calculateSimilarity(organization, visitorOrganizations);

                    log.info("Weekly Stats | PersonaReq={} | Initiated={} | Enquiries={} | Partnerships={} | Messages={} | IPP={}",
                            personaRequested, partnershipInitiated, enquiries, newPartnerShips, communicated, ippRate);

                    Summary summary = Summary.builder()
                            .personaRequested(String.valueOf(personaRequested))
                            .newPartnerships(String.valueOf(newPartnerShips))
                            .partnershipInitiated(String.valueOf(partnershipInitiated))
                            .ippRate(ippRate + "%")
                            .totalEnquiries(String.valueOf(enquiries))
                            .partnersCommunicated(String.valueOf(communicated))
                            .build();

                    TemplateData templateData = TemplateData.builder()
                            .templateCode("weekly_outcome")
                            .orgName(orgName)
                            .tasks(tasks)
                            .progress(progress)
                            .summary(summary)
                            .build();

                    log.info("Loading email template: {}", templateData.getTemplateCode());

                    EmailTemplate template = emailFromTemplateService.getTemplateByCode(templateData.getTemplateCode());
                    template = replaceJavaTags(template, templateData);

                    List<EmailReqModelWithMultipartAttachments> emailRequests =
                            emailFromTemplateService.prepareEmailsForOrganizations(template, List.of(orgId), null);

                    log.info("Prepared {} email(s) for org {}", emailRequests.size(), orgName);

                    emailService.sendMultiple(emailRequests, templateData.getTemplateCode(), 0L, 0L);
                    log.info("Email successfully sent to organization {}", orgName);

                } catch (Exception ex) {
                    log.error("Failed to process organization ID {} | Name {}", orgId, orgName, ex);
                }

            });

        } catch (Exception e) {
            log.error("Weekly Outcome Scheduler CRITICAL FAILURE", e);
            throw new RuntimeException(e);
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Weekly Outcome Email job completed in {} ms", duration);
    }

    public static EmailTemplate replaceJavaTags(EmailTemplate template, TemplateData data) {
        log.debug("Replacing tags for org {}", data.getOrgName());

        String htmlContent = template.getBodyHtml();
        Progress progress = data.getProgress();

        htmlContent = htmlContent.replace(PROGRESS_FRACTION, progress.getFraction())
                .replaceAll("width: 50%;", "width: " + progress.getPercentage() + "%;");

        htmlContent = htmlContent.replace(ORG_NAME, data.getOrgName());

        if (htmlContent.contains(TASKS_LIST)) {
            htmlContent = htmlContent.replace(TASKS_LIST, generateTasksList(data.getTasks()));
        }

        Summary stats = data.getSummary();
        if (stats != null) {
            htmlContent = htmlContent
                    .replace(PERSONA_REQUESTED, stats.getPersonaRequested())
                    .replace(NEW_PARTNERSHIPS, stats.getNewPartnerships())
                    .replace(PARTNERSHIP_INITIATED, stats.getPartnershipInitiated())
                    .replace(IPP_RATE, stats.getIppRate())
                    .replace(TOTAL_ENQUIRIES, stats.getTotalEnquiries())
                    .replace(PARTNERS_COMMUNICATED, stats.getPartnersCommunicated());
        }

        template.setBodyHtml(htmlContent);
        log.debug("Template populated for org {}", data.getOrgName());
        return template;
    }



    private static String generateTasksList(List<Task> tasks) {
        StringBuilder tasksHtml = new StringBuilder();
        for (Task task : tasks) {
            tasksHtml.append(createTaskHtml(task));
        }
        return tasksHtml.toString();
    }

    private static String createTaskHtml(Task task) {
        String style = task.isCompleted()
                ? "text-decoration: line-through; color: #999;"
                : "color: #999;";

        String dotColor = task.isCompleted() ? "#999" : "#4caf50";

        return String.format("""
                <li style="display: flex; align-items: center; gap: 10px; font-size: 14px; %s margin-bottom: 10px; padding-bottom:10px; border-bottom: 1px solid #ddd;">
                    <span style="width: 14px; height: 14px; margin-right: 5px; background-color: %s; border-radius: 50%%;"></span>
                    %s
                </li>
                """, style, dotColor, task.getName());
    }

    private double calculateSimilarity(Organization mainOrg, List<Long> visitorOrgIds) {

        log.debug("Calculating IPP for org {} with {} visitors",
                mainOrg.getId(), visitorOrgIds.size());

        Set<Object> mainPreferences = new HashSet<>();
        mainPreferences.addAll(mainOrg.getPreferredPartnershipTypes());
        mainPreferences.addAll(mainOrg.getPreferredSectors());

        if (mainPreferences.isEmpty() || visitorOrgIds.isEmpty()) {
            log.warn("No preferences/visitors found for org {}", mainOrg.getId());
            return 0.0;
        }

        Set<Object> allVisitorPreferences = new HashSet<>();
        visitorOrgIds.forEach(id -> {
            Organization visitorOrg = organizationRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Visitor org not found: " + id));
            allVisitorPreferences.addAll(visitorOrg.getPreferredPartnershipTypes());
            allVisitorPreferences.addAll(visitorOrg.getPreferredSectors());
        });

        Set<Object> commonPreferences = new HashSet<>(mainPreferences);
        commonPreferences.retainAll(allVisitorPreferences);

        double score = (commonPreferences.size() * 100.0) /
                Math.max(mainPreferences.size(), allVisitorPreferences.size());

        log.info("IPP Score for org {} → {}%", mainOrg.getName(), score);

        return score;
    }


}

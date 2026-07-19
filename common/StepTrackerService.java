package com.sharkdom.service.common;

import com.sharkdom.config.CSMAlertProperties;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.entity.organization.OrganizationUserMapping;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.model.common.StepTracker;
import com.sharkdom.repository.common.StepTrackerRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.service.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StepTrackerService {

    private static final String NOT_UPDATED = "NOT UPDATED";

    @Autowired
    private StepTrackerRepository stepTrackerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationUserMappingRepository organizationUserMappingRepository;

    @Autowired
    private CSMAlertProperties csmAlertProperties;

    @Autowired
    private TaskScheduler taskScheduler;

    @Value("${env}")
    private String env;

    // =========================
    // STEP TRACKING
    // =========================

    public StepTracker addStep(StepTracker stepTracker) {

        log.info("Adding step | userId={}", stepTracker.getUserId());

        Optional<StepTracker> optStepTracker =
                stepTrackerRepository.findByUserId(stepTracker.getUserId());

        if (optStepTracker.isPresent()) {
            StepTracker existing = optStepTracker.get();

            updateSteps(existing, stepTracker);

            log.info("Updating StepTracker | userId={}", stepTracker.getUserId());
            return stepTrackerRepository.save(existing);
        }

        log.info("Creating StepTracker | userId={}", stepTracker.getUserId());
        return stepTrackerRepository.save(stepTracker);
    }

    private void updateSteps(StepTracker existing, StepTracker incoming) {

        if (incoming.getStepOneCompleted() != null)
            existing.setStepOneCompleted(incoming.getStepOneCompleted());

        if (incoming.getStepTwoCompleted() != null)
            existing.setStepTwoCompleted(incoming.getStepTwoCompleted());

        if (incoming.getStepThreeCompleted() != null)
            existing.setStepThreeCompleted(incoming.getStepThreeCompleted());

        if (incoming.getStepFourCompleted() != null)
            existing.setStepFourCompleted(incoming.getStepFourCompleted());

        if (incoming.getStepFiveCompleted() != null)
            existing.setStepFiveCompleted(incoming.getStepFiveCompleted());

        if (incoming.getStepSixCompleted() != null)
            existing.setStepSixCompleted(incoming.getStepSixCompleted());

        if (incoming.getStepSevenCompleted() != null)
            existing.setStepSevenCompleted(incoming.getStepSevenCompleted());

        if (incoming.getStepEightCompleted() != null)
            existing.setStepEightCompleted(incoming.getStepEightCompleted());

        if (incoming.getStepNineCompleted() != null) {

            if (Boolean.TRUE.equals(incoming.getStepNineCompleted())) {
                csmTeamAlert(existing.getUserId());
            }

            existing.setStepNineCompleted(incoming.getStepNineCompleted());
        }
    }

    // =========================
    // STEP 9 LOGIC
    // =========================

    public void csmTeamAlert(String userId) {

        // Mark user onboarded
        userRepository.findByUserId(userId)
                .ifPresent(user -> {
                    user.setOnboarded(true);
                    userRepository.save(user);
                });

        // Schedule CSM email
        organizationUserMappingRepository.findByUserId(userId)
                .ifPresent(mapping -> {
                    log.info("Step 9 completed — Scheduling CSM email | userId={} | orgId={}",
                            mapping.getUserId(),
                            mapping.getOrganizationId());

                    scheduleEmail(
                            "CSMAlert",
                            mapping,
                            30
                    );
                });
    }

    // =========================
    // EMAIL SCHEDULING
    // =========================

    public void scheduleEmail(String templateCode,
                              OrganizationUserMapping organizationUserMapping,
                              long delayMinutes) {

        Long orgId = organizationUserMapping.getOrganizationId();


        if (orgId == null || orgId == 0L) {
            log.warn("Invalid orgId — skipping CSM email | userId={}",
                    organizationUserMapping.getUserId());
            return;
        }

        Instant runAt = Instant.now().plus(delayMinutes, ChronoUnit.MINUTES);

        log.info("CSM email scheduled | template={} | orgId={} | userId={} | runAt={}",
                templateCode,
                orgId,
                organizationUserMapping.getUserId(),
                runAt);

        taskScheduler.schedule(() -> {
            try {
                sendEmail(templateCode, organizationUserMapping);
            } catch (Exception ex) {
                log.error("Failed to send scheduled CSM email | orgId={} | userId={}",
                        orgId,
                        organizationUserMapping.getUserId(),
                        ex);
            }
        }, runAt);
    }

    // =========================
    // EMAIL SENDING
    // =========================

    private void sendEmail(String templateCode,
                           OrganizationUserMapping organizationUserMapping) {

        if (csmAlertProperties.getEmail() == null ||
                csmAlertProperties.getEmail().isBlank()) {

            log.warn("CSM alert email not configured — skipping send");
            return;
        }

        log.info("Sending CSM email | template={} | orgId={} | userId={}",
                templateCode,
                organizationUserMapping.getOrganizationId(),
                organizationUserMapping.getUserId());

        var orgOpt = organizationRepository.findById(
                organizationUserMapping.getOrganizationId()
        );

        var userOpt = userRepository.findByUserId(
                organizationUserMapping.getUserId()
        );

        if (orgOpt.isEmpty() || userOpt.isEmpty()) {
            log.warn("Organization or User not found — Email skipped | orgId={} | userId={}",
                    organizationUserMapping.getOrganizationId(),
                    organizationUserMapping.getUserId());
            return;
        }

        var org = orgOpt.get();
        var user = userOpt.get();

        Map<String, String> map = new HashMap<>();

        map.put("organization.name", safe(org.getName()));
        map.put("answer1", safe(user.getName()));
        map.put("answer2", safe(org.getWebsite()));
        map.put("answer3", safe(org.getName()));
        map.put("answer4", safe(user.getUserType()));
        map.put("answer5", safe(org.getTargetMarket()));
        map.put("answer6", safeEnum(org.getPartnershipTeamSize()));
        map.put("answer7", safe(org.getOnboardedPartners()));
        map.put("answer8", safeList(org.getGoalsToUseSharkdom()));
        map.put("answer9", safeList(org.getRegionToPartnerWith()));
        map.put("answer10", safeList(org.getPreferredPartnershipTypes()));

        emailService.sendBusinessAlertEmail(
                templateCode,
                csmAlertProperties.getEmail(),
                map
        );

        log.info("CSM email sent | orgId={} | userId={}",
                organizationUserMapping.getOrganizationId(),
                organizationUserMapping.getUserId());
    }

    // =========================
    // HELPERS
    // =========================

    public StepTracker getStepTrackerByUserId(String userId) {

        if (userId == null || userId.isBlank()) {
            throw new ServiceException(ErrorMessages.SH39, "null");
        }

        try {
            return stepTrackerRepository.findByUserId(userId)
                    .orElseThrow(() ->
                            new ServiceException(ErrorMessages.SH39, userId)
                    );
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException(ErrorMessages.SH116, ex.getMessage());
        }
    }

    private String safe(Object value) {
        return Optional.ofNullable(value)
                .map(Object::toString)
                .filter(s -> !s.isBlank())
                .orElse(NOT_UPDATED);
    }

    private String safeEnum(Enum<?> value) {
        return value != null ? value.name() : NOT_UPDATED;
    }

    private String safeList(java.util.List<?> list) {
        if (list == null || list.isEmpty()) {
            return NOT_UPDATED;
        }

        String result = list.stream()
                .map(Object::toString)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", "));

        return result.isEmpty() ? NOT_UPDATED : result;
    }
}

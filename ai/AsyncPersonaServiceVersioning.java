package com.sharkdom.service.ai;

import com.sharkdom.config.WebSocketHandler;
import com.sharkdom.entity.ai.PersonaDetailsEntity;
import com.sharkdom.entity.ai.PersonaEntity;
import com.sharkdom.entity.ai.PersonaStatusEntity;
import com.sharkdom.entity.ai.PersonaUserNotifyEntity;
import com.sharkdom.entity.notification.Notification;
import com.sharkdom.model.PersonaStatus;
import com.sharkdom.model.ai.PercentageCategory;
import com.sharkdom.model.ai.PersonaModelResponse;
import com.sharkdom.model.ai.PersonaRequest;
import com.sharkdom.model.email.TemplateEmailReqModel;
import com.sharkdom.model.email.TemplateOrganizationEmailReqModel;
import com.sharkdom.model.user.UserEmailId;
import com.sharkdom.repository.ai.PersonaDetailsRepository;
import com.sharkdom.repository.ai.PersonaRepository;
import com.sharkdom.repository.ai.PersonaStatusRepository;
import com.sharkdom.repository.ai.PersonaUserNotifyRepository;
import com.sharkdom.repository.notification.NotificationRepository;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.service.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AsyncPersonaServiceVersioning {

    @Value("${persona.url}")
    private String personaUrl;

    @Value("${env}")
    private String env;

    private final RestTemplate restTemplate;
    private final EmailService emailService;
    private final WebSocketHandler webSocketHandler;
    private final NotificationRepository notificationRepository;
    private final PersonaRepository personaRepository;
    private final PersonaDetailsRepository personaDetailsRepository;
    private final PersonaStatusRepository personaStatusRepository;
    private final PersonaUserNotifyRepository personaUserNotifyRepository;
    private final UserRepository userRepository;

    public AsyncPersonaServiceVersioning(EmailService emailService,
                                         WebSocketHandler webSocketHandler,
                                         NotificationRepository notificationRepository,
                                         PersonaRepository personaRepository,
                                         PersonaDetailsRepository personaDetailsRepository,
                                         PersonaStatusRepository personaStatusRepository,
                                         PersonaUserNotifyRepository personaUserNotifyRepository,
                                         UserRepository userRepository) {
        this.emailService = emailService;
        this.webSocketHandler = webSocketHandler;
        this.notificationRepository = notificationRepository;
        this.personaRepository = personaRepository;
        this.personaDetailsRepository = personaDetailsRepository;
        this.personaStatusRepository = personaStatusRepository;
        this.personaUserNotifyRepository = personaUserNotifyRepository;
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
    }

    @Async
    @Transactional
    public void savePersona(PersonaRequest personaRequest,
                            Integer version,
                            Integer versionId) {

        log.info("Starting persona generation orgId={} version={} versionId={}",
                personaRequest.getOrganizationId(),
                version,
                versionId);

        callPersona(personaRequest, version, versionId);
    }


    public void callPersona(PersonaRequest personaRequest,
                            Integer version,
                            Integer versionId) {
        log.info("Calling persona API");

        // filter valid domains only
        List<String> validSites = Optional.ofNullable(personaRequest.getSites())
                .orElse(Collections.emptyList())
                .stream()
                .filter(site -> site != null && !site.isBlank())
                .toList();

        if (validSites.isEmpty()) {
            log.warn("No valid domains found, skipping persona API call");
            return;
        }

        Map<String, List<String>> postRequest = new HashMap<>();
        postRequest.put("urls", validSites);

        HttpEntity<Map<String, List<String>>> requestEntity = new HttpEntity<>(postRequest);

        ParameterizedTypeReference<List<PersonaModelResponse>> responseType =
                new ParameterizedTypeReference<>() {};

        List<PersonaModelResponse> responseEntity;

        try {
            responseEntity = restTemplate
                    .exchange(personaUrl, HttpMethod.POST, requestEntity, responseType)
                    .getBody();
        } catch (Exception e) {
            log.error("Error calling persona API", e);
            return;
        }

        if (responseEntity == null || responseEntity.isEmpty()) {
            log.warn("Persona API returned empty response");
            return;
        }

        savePersonaDetails(responseEntity, personaRequest.getOrganizationId(), version, versionId);
        calculateAndSavePercentages(responseEntity, personaRequest.getOrganizationId(), version, versionId);
        savePersonaStatus(personaRequest.getOrganizationId(), version, versionId);
    }

    private void savePersonaDetails(List<PersonaModelResponse> responses,
                                    Long organizationId,
                                    Integer version,
                                    Integer versionId) {

        List<PersonaDetailsEntity> details = responses.stream()
                .map(res -> PersonaDetailsEntity.builder()
                        .organizationId(organizationId)
                        .companySector(res.getCompanySector())
                        .companySize(res.getCompanySize())
                        .marketSegment(res.getMarketSegment())
                        .isPartnershipProgram(res.getIsPartnershipProgram())
                        .version(version)
                        .versionId(versionId)
                        .build())
                .toList();

        personaDetailsRepository.saveAll(details);
    }



    private void calculateAndSavePercentages(List<PersonaModelResponse> responses,
                                             Long organizationId,
                                             Integer version,
                                             Integer versionId) {

        int total = responses.size();

        Map<String, List<PercentageCategory>> percentages = new HashMap<>();

        percentages.put("companySector",
                calculatePercentage(responses, PersonaModelResponse::getCompanySector, total));

        percentages.put("companySize",
                calculatePercentage(responses, PersonaModelResponse::getCompanySize, total));

        percentages.put("isPartnershipProgram",
                calculatePercentage(responses, PersonaModelResponse::getIsPartnershipProgram, total));

        percentages.put("marketSegment",
                calculatePercentage(responses, PersonaModelResponse::getMarketSegment, total));

        savePercentagesToDb(percentages, organizationId, version, versionId);
    }

    private List<PercentageCategory> calculatePercentage(
            List<PersonaModelResponse> personaModelResponses,
            Function<PersonaModelResponse, String> classifier,
            int totalCount) {

        Map<String, Long> counts = personaModelResponses.stream()
                .flatMap(response -> {
                    String value = classifier.apply(response);

                    // NULL SAFE SPLIT FIX
                    return Arrays.stream(Optional.ofNullable(value)
                                    .orElse("")
                                    .split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty());
                })
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return counts.entrySet().stream()
                .map(entry -> new PercentageCategory(
                        entry.getKey(),
                        (entry.getValue() * 100.0) / totalCount))
                .collect(Collectors.toList());
    }

    private void savePercentagesToDb(Map<String, List<PercentageCategory>> percentages,
                                     Long organizationId) {

        for (Map.Entry<String, List<PercentageCategory>> entry : percentages.entrySet()) {
            String attribute = entry.getKey();

            for (PercentageCategory categoryEntry : entry.getValue()) {
                PersonaEntity record = PersonaEntity.builder()
                        .organizationId(organizationId)
                        .category(categoryEntry.getKey())
                        .percentage(categoryEntry.getPercentage())
                        .attribute(attribute)
                        .build();

                personaRepository.save(record);
            }
        }

        var response = personaStatusRepository.getByOrganizationId(organizationId);

        if (response != null) {
            response.setPersonaStatus(PersonaStatus.COMPLETED);
            personaStatusRepository.save(response);
        }

        if (response != null && PersonaStatus.COMPLETED.equals(response.getPersonaStatus())) {
            log.info("Sending persona notify emails");
            updateOtherOrgsUsers(organizationId);
        }
    }


    private void savePercentagesToDb(Map<String, List<PercentageCategory>> percentages,
                                     Long organizationId,
                                     Integer version,
                                     Integer versionId) {

        List<PersonaEntity> records = new ArrayList<>();

        for (Map.Entry<String, List<PercentageCategory>> entry : percentages.entrySet()) {

            String attribute = entry.getKey();

            for (PercentageCategory category : entry.getValue()) {

                records.add(PersonaEntity.builder()
                        .organizationId(organizationId)
                        .attribute(attribute)
                        .category(category.getKey())
                        .percentage(category.getPercentage())
                        .version(version)
                        .versionId(versionId)
                        .build());
            }
        }

        personaRepository.saveAll(records);
    }

    private void savePersonaStatus(Long organizationId,
                                   Integer version,
                                   Integer versionId) {

        PersonaStatusEntity status =
                personaStatusRepository
                        .findTopByOrganizationIdAndVersionIdOrderByIdDesc(
                                organizationId, versionId);

        if (status != null) {
            status.setPersonaStatus(PersonaStatus.COMPLETED);
            personaStatusRepository.save(status);
        }
    }

//    private void savePersonaStatus(Long organizationId,
//                                   Integer version,
//                                   Integer versionId) {
//
//        PersonaStatusEntity status = new PersonaStatusEntity();
//
//        status.setOrganizationId(organizationId);
//        status.setVersion(version);
//        status.setVersionId(versionId);
//        status.setPersonaStatus(PersonaStatus.COMPLETED);
//
//        personaStatusRepository.save(status);
//
//        updateOtherOrgsUsers(organizationId);
//    }

    private void sendCompletionNotification(Long orgId) {

        TemplateOrganizationEmailReqModel reqModel =
                TemplateOrganizationEmailReqModel.builder()
                        .templateCode("Persona_Created")
                        .organizationIds(List.of(orgId))
                        .build();

        emailService.sendByTemplateAndOrganizationIds(reqModel, null, 1L, 1L);

        Notification notification = Notification.builder()
                .subject("Persona Created")
                .body("Congratulations! Your persona has been created.")
                .forWeb(true)
                .organizationId(orgId)
                .build();

        webSocketHandler.sendMessageToUser(orgId, notification);
        notificationRepository.save(notification);
    }

    private void updateOtherOrgsUsers(Long organizationId) {

        List<PersonaUserNotifyEntity> notifyEntities =
                personaUserNotifyRepository.findAllByRecieverOrgIdAndIsNotified(organizationId, false);

        notifyEntities.forEach(entity -> {
            try {
                notifyUsersUsingOrgId(entity.getSenderOrgId());
                entity.setIsNotified(true);
                personaUserNotifyRepository.save(entity);
            } catch (Exception e) {
                log.error("Failed notifying users", e);
            }
        });
    }

    private void notifyUsersUsingOrgId(Long orgId) {

        List<UserEmailId> users = userRepository.getAllUsersByOrganizationId(orgId);

        final String templateCode = "Partner_Persona_Notify";

        users.forEach(user ->
                emailService.sendByTemplateAndUserIds(
                        TemplateEmailReqModel.builder()
                                .templateCode(templateCode)
                                .userIds(List.of(user.getUserId()))
                                .username(user.getName())
                                .build(),
                        null
                )
        );
    }
}
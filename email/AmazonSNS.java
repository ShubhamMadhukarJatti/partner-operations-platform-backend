package com.sharkdom.service.email;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sharkdom.entity.ai.PersonaNotifyEntity;
import com.sharkdom.entity.email.EmailStatistics;
import com.sharkdom.model.email.AllCampaignStats;
import com.sharkdom.repository.ai.PersonaNotifyRepository;
import com.sharkdom.repository.email.EmailStatisticsRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AmazonSNS {
    private final Gson gson;
    private final EmailStatisticsRepository emailStatisticsRepository;
    private final RestTemplate restTemplate;
    private final OrganizationRepository organizationRepository;
    private final PersonaNotifyRepository personaNotifyRepository;
    private final EmailService emailService;
    @Value("${env}")
    private String configEnv;

    public AmazonSNS(Gson gson, EmailStatisticsRepository emailStatisticsRepository, OrganizationRepository organizationRepository, PersonaNotifyRepository personaNotifyRepository,EmailService emailService) {
        this.gson = gson;
        this.emailStatisticsRepository = emailStatisticsRepository;
        this.organizationRepository = organizationRepository;
        this.personaNotifyRepository = personaNotifyRepository;
        restTemplate = new RestTemplate();
        this.emailService =  emailService;
    }

    public void saveSNSData(String data) {
        try {
            JsonObject rootObject = gson.fromJson(data, JsonObject.class);
            String eventType = rootObject.get("eventType").getAsString();
            var headers = rootObject.get("mail").getAsJsonObject().get("headers").getAsJsonArray();
            var sentAtString = rootObject.get("mail").getAsJsonObject().get("timestamp").getAsString();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = dateFormat.parse(sentAtString);
            Instant instant = date.toInstant();
            LocalDate sentAt = instant.atZone(ZoneId.systemDefault()).toLocalDate();

            String to = "";
            String templateCode = "";
            String subject = "";
            String env = "";
            long orgCollaborationId = 0L;

            // Extracting the required values from headers
            for (var header : headers) {
                JsonObject headerObj = header.getAsJsonObject();
                if ("To".equalsIgnoreCase(headerObj.get("name").getAsString())) {
                    to = headerObj.get("value").getAsString();
                } else if ("templateCode".equalsIgnoreCase(headerObj.get("name").getAsString())) {
                    templateCode = headerObj.get("value").getAsString();
                } else if ("subject".equalsIgnoreCase(headerObj.get("name").getAsString())) {
                    subject = headerObj.get("value").getAsString();
                } else if ("env".equalsIgnoreCase(headerObj.get("name").getAsString())) {
                    env = headerObj.get("value").getAsString();
                } else if ("collaborationId".equalsIgnoreCase(headerObj.get("name").getAsString())) {
                    orgCollaborationId = headerObj.get("value").getAsLong();
                }
            }

            // Check if the header env matches the config env
            if (!Objects.equals(configEnv, env)) {
                //log.info("Env mismatch: expected {}, but got {}. Skipping save.", configEnv, env);
                return;
            }

            if ("open".equalsIgnoreCase(eventType)) {
                var timestamp = rootObject.get("open").getAsJsonObject().get("timestamp").getAsString();
                Date openedAt = dateFormat.parse(timestamp);
                EmailStatistics emailStatistics = EmailStatistics.builder()
                        .eventType(eventType)
                        .templateCode(templateCode)
                        .email(to)
                        .subject(subject)
                        .env(env)
                        .sentAt(sentAt)
                        .openedAt(openedAt)
                        .build();
                emailStatisticsRepository.save(emailStatistics);
                if ("notify_persona".equalsIgnoreCase(templateCode)) {
                    PersonaNotifyEntity personaNotifyEntity = PersonaNotifyEntity.builder()
                            .eventType(eventType)
                            .receiverOrganizationId(organizationRepository.findIdByEmail(to))
                            .sentAt(sentAt)
                            .openedAt(openedAt)
                            .senderOrganizationId(orgCollaborationId).build();
                    personaNotifyRepository.save(personaNotifyEntity);
                }
                sendCollaborationRequests(orgCollaborationId, templateCode, env, "isEmailOpened");
            } else if ("click".equalsIgnoreCase(eventType)) {
                var timestamp = rootObject.get("click").getAsJsonObject().get("timestamp").getAsString();
                var clickedLink = rootObject.get("click").getAsJsonObject().get("link").getAsString();
                Date clickedAt = dateFormat.parse(timestamp);
                EmailStatistics emailStatistics = EmailStatistics.builder()
                        .eventType(eventType)
                        .templateCode(templateCode)
                        .email(to)
                        .subject(subject)
                        .env(env)
                        .clickedAt(clickedAt)
                        .clickedLink(clickedLink)
                        .sentAt(sentAt)
                        .build();
                emailStatisticsRepository.save(emailStatistics);
                if ("notify_persona".equalsIgnoreCase(templateCode)) {
                    PersonaNotifyEntity personaNotifyEntity = PersonaNotifyEntity.builder()
                            .eventType(eventType)
                            .receiverOrganizationId(organizationRepository.findIdByEmail(to))
                            .clickedAt(clickedAt)
                            .clickedLink(clickedLink)
                            .sentAt(sentAt)
                            .senderOrganizationId(orgCollaborationId).build();
                    personaNotifyRepository.save(personaNotifyEntity);
                }
                sendCollaborationRequests(orgCollaborationId, templateCode, env, "isEmailClicked");
            } else if ("Bounce".equalsIgnoreCase(eventType)) {
                EmailStatistics emailStatistics = EmailStatistics.builder()
                        .eventType(eventType)
                        .templateCode(templateCode)
                        .email(to)
                        .subject(subject)
                        .sentAt(sentAt)
                        .env(env)
                        .build();
                emailStatisticsRepository.save(emailStatistics);
//          //      forwardBounceToNoreply(
//                        templateCode,
//                        subject,
//                        to,
//                        sentAt,
//                        env,
//                        eventType
//                );
            }
        } catch (Exception e) {
            log.error("unable to save sns data {}", e.getMessage());
        }
    }

    public Page<EmailStatistics> getEmailStatistics(String eventType, String env, String templateCode, String sentAt, int size, int page) {
        LocalDate localDate = LocalDate.parse(sentAt);
        return emailStatisticsRepository.getAllByEventTypeAndEnvAndTemplateCodeAndSentAt(eventType, env, templateCode, localDate, PageRequest.of(page, size));
    }

    public Page<AllCampaignStats> getAllCampaignStatistics(int page, int size) {
        return emailStatisticsRepository.getAllCampaignStats(PageRequest.of(page, size));
    }

    private void sendCollaborationRequests(long orgCollaborationId, String templateCode, String env, String action) {
        String baseUrl = Objects.equals(env, "dev") ? "https://dev.sharkdomapi.com" : "https://prod.sharkdomapi.com";
        sendRequest(baseUrl + "/organizationCollaboration/" + action + "?orgCollaborationId=" + orgCollaborationId);
        if (Objects.equals(templateCode, "Signer_Receiver")) {
            sendRequest(baseUrl + "/organizationCollaboration/timeline?orgCollaborationId=" + orgCollaborationId + "&template=" + templateCode);
        }
    }

    private void sendRequest(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Custom-header", "sharkdom-header");
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ParameterizedTypeReference<Map<Object, Object>> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<Map<Object, Object>> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);
        System.out.println(response.getBody());
    }
//    private void forwardBounceToNoreply(
//            String templateCode,
//            String subject,
//            String to,
//            LocalDate sentAt,
//            String env,
//            String eventType
//    ) {
//        try {
//            StringBuilder body = new StringBuilder();
//            body.append("🚫 Email bounce detected\n\n");
//            body.append("Template Code: ").append(templateCode).append("\n");
//            body.append("Original Subject: ").append(subject).append("\n");
//            body.append("Sent To: ").append(to).append("\n");
////            body.append("Sender: ").append(source).append("\n");
////            body.append("Message ID: ").append(messageId).append("\n");
////            body.append("Bounce Time: ").append(bounceTimestamp).append("\n");
////            body.append("Bounce Type: ").append(bounceType).append("\n");
////            body.append("Bounce SubType: ").append(bounceSubType).append("\n\n");
////            body.append("Bounced Recipients: ").append(String.join(", ", bouncedRecipients)).append("\n");
//            body.append("Environment: ").append(env).append("\n");
//            body.append("Event Type: ").append(eventType).append("\n");
//            body.append("Sent At: ").append(sentAt).append("\n");
//
//            String forwardSubject = "Forwarded Bounce: " + subject;
//            emailService.sendEmail("ayush@sharkdom.com", forwardSubject, body.toString());
//
//        } catch (Exception e) {
//            log.error("Error forwarding bounced email to noreply", e);
//        }
//    }



}

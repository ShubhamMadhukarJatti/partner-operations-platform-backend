package com.sharkdom.service.user;

import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.organization.OrganizationAvailabilityRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class SlackService {

    @Value("${slack.client-id}")
    private String clientId;

    @Value("${slack.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final IntegrationRepository integrationRepository;
    private final OrganizationUserMappingRepository organizationUserMappingRepository;
    private final OrganizationAvailabilityRepository organizationAvailabilityRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public SlackService(IntegrationRepository integrationRepository, OrganizationUserMappingRepository organizationUserMappingRepository, OrganizationAvailabilityRepository organizationAvailabilityRepository, UserRepository userRepository, OrganizationRepository organizationRepository) {
        this.integrationRepository = integrationRepository;
        this.organizationUserMappingRepository = organizationUserMappingRepository;
        this.organizationAvailabilityRepository = organizationAvailabilityRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    public void sendAvailability(String userId, String channelId) {
        var currentUser = integrationRepository.findByUserIdAndIntegrationType(userId, IntegrationType.SLACK);
        var availability = organizationAvailabilityRepository.findByOrganizationId(currentUser.getOrganizationId());
        if (availability.isPresent()) {
            return;
        }
        var orgName = organizationRepository.findNameById(currentUser.getOrganizationId());

        try {
            JSONObject payload = new JSONObject();
            payload.put("channel", channelId);
            JSONArray blocks = new JSONArray();

            // Add the introductory text
            String introText = String.format("Hi Slack User, set up availability hours for %s for your upcoming partner to book a meet to increase your chances of establishing partnership at a 39%% faster rate.", orgName);
            blocks.put(new JSONObject()
                    .put("type", "section")
                    .put("text", new JSONObject()
                            .put("type", "mrkdwn")
                            .put("text", introText)
                    )
            );


            blocks.put(new JSONObject()
                    .put("type", "actions")
                    .put("elements", new JSONArray()
                            .put(new JSONObject()
                                    .put("type", "button")
                                    .put("text", new JSONObject()
                                            .put("type", "plain_text")
                                            .put("text", "Open Preferences in Sharkdom")
                                    )
                                    .put("url", "https://sharkdom.com/dashboard/preferences")
                                    .put("action_id", "open_preferences")
                            )
                    )
            );


            payload.put("blocks", blocks);

            // Send the message using RestTemplate
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + currentUser.getRefreshToken());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(payload.toString(), headers);

            String url = "https://slack.com/api/chat.postMessage";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Message sent successfully!");
            } else {
                System.out.println("Failed to send message: " + response.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendProposalRequest(Long collabId, String userId, String channelId) {
        var currentUser = integrationRepository.findByUserIdAndIntegrationType(userId, IntegrationType.SLACK);

        try {
            JSONObject payload = new JSONObject();
            payload.put("channel", channelId);

            // Blocks with Timepickers for Sunday to Saturday
            JSONArray blocks = new JSONArray();

            // Add the introductory text
            String introText = "Congrats on adding new partnership in your pipeline. You can check your progress here";
            blocks.put(new JSONObject()
                    .put("type", "section")
                    .put("text", new JSONObject()
                            .put("type", "mrkdwn")
                            .put("text", introText)
                    )
            );


            blocks.put(new JSONObject().put("type", "divider"));
            var link = String.format("https://sharkdom.com/dashboard/%s", collabId);
            // Add the "Open Link" and "Submit Availability" buttons
            blocks.put(new JSONObject()
                    .put("type", "actions")
                    .put("elements", new JSONArray()
                            .put(new JSONObject()
                                    .put("type", "button")
                                    .put("text", new JSONObject()
                                            .put("type", "plain_text")
                                            .put("text", "Open Partnership in Sharkdom")
                                    )
                                    .put("url", link)
                                    .put("action_id", "open_link")
                            )
                    )
            );

            payload.put("blocks", blocks);

            // Send the message using RestTemplate
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + currentUser.getRefreshToken());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(payload.toString(), headers);

            String url = "https://slack.com/api/chat.postMessage";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            log.error("slack response:" + response.getBody());
            if (response.getStatusCode() == HttpStatus.OK) {
                log.error("Message sent successfully!");
            } else {
                log.error("Failed to send message: " + response.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

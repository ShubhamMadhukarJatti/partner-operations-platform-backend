package com.sharkdom.service.dweep;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organizationcollaboration.OrganizationCollaboration;
import com.sharkdom.model.ai.SharkqQueryRequest;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class ProposalService {

    @Autowired
    private OrganizationRepository organizationRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public OrganizationCollaboration sendProposal(SharkqQueryRequest request, String accessToken) {
        Long senderOrgId = Util.getOrgIdFromToken();
        log.info("Sender Organization ID: {}, Request: {}", senderOrgId, request);

        if (request.getPromptId() != 4 || request.getOrgIdList() == null || request.getOrgIdList().isEmpty()) {
            log.warn("Invalid promptId or empty orgIdList in request. Skipping proposal.");
            return null;
        }

        List<Organization> targetOrganizations = organizationRepository.findAllByIdIn(request.getOrgIdList());
        log.info("Fetched {} organizations for collaboration proposal", targetOrganizations.size());

        OrganizationCollaboration lastCreatedProposal = null;

        for (Organization targetOrg : targetOrganizations) {
            log.info("Sending proposal to organization: {}", targetOrg.getName());
            lastCreatedProposal = exchangeOrgCollab(targetOrg, senderOrgId, accessToken);
        }

        return lastCreatedProposal;
    }

    private OrganizationCollaboration exchangeOrgCollab(Organization receiverOrg, Long senderOrgId, String accessToken) {
        log.info("Initiating exchangeOrgCollab for Receiver Org ID: {}", receiverOrg.getId());

        String senderUserId = getMappingsByOrganizationId(senderOrgId, accessToken);
        String receiverUserId = getMappingsByOrganizationId(receiverOrg.getId(), accessToken);

        Map<String, Object> payload = new HashMap<>();
        payload.put("senderOrganizationId", senderOrgId);
        payload.put("receiverOrganizationId", receiverOrg.getId());
        payload.put("senderOrganizationName", "string");
        payload.put("receiverOrganizationName", receiverOrg.getName());
        payload.put("senderUserId", senderUserId);
        payload.put("acceptorUserId", receiverUserId);
        payload.put("status", "string");
        payload.put("senderUrlsJson", "string");
        payload.put("receiverUrlsJson", "string");
        payload.put("chatAccessAllowed", true);
        payload.put("contactPersonUserId", "string");
        payload.put("collaborationCategory", "RELIABLE_PARTNER");
        payload.put("receiverBenefits", getBenefitsList("TWOB"));
        payload.put("senderBenefit", getBenefitsList("GetBee"));
        payload.put("senderOrgmodifiedByUserId", "string");
        payload.put("receiverOrgmodifiedByUserId", "string");
        payload.put("emailOpened", true);
        payload.put("emailClicked", true);
        payload.put("viewed", true);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<OrganizationCollaboration> response = restTemplate.exchange(
                    "https://dev.sharkdomapi.com/organizationCollaboration",
                    HttpMethod.POST,
                    entity,
                    OrganizationCollaboration.class
            );

            log.info("Proposal created successfully: HTTP {}", response.getStatusCode());
            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to create organization collaboration", e);
            return null;
        }
    }

    private List<Map<String, Object>> getBenefitsList(String orgName) {
        return List.of(
                Map.of(
                        "benefit", orgName + " should provide data analytics...",
                        "description", "Utilizing data...",
                        "activeConversation", false,
                        "status", "ACTIVE"
                ),
                Map.of(
                        "benefit", orgName + " must offer technology solutions...",
                        "description", "Implementing automation...",
                        "activeConversation", false,
                        "status", "ACTIVE"
                )
        );
    }

    private String getMappingsByOrganizationId(Long orgId, String accessToken) {
        String url = "https://dev.sharkdomapi.com/orgUserMapping/allByOrganizationId?id=" + orgId;
        log.info("Calling getMappingsByOrganizationId for Org ID: {}", orgId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            String json = response.getBody();
            ObjectMapper mapper = new ObjectMapper();

            List<Map<String, Object>> userList = mapper.readValue(json, new TypeReference<>() {});
            for (Map<String, Object> item : userList) {
                Map<String, Object> user = (Map<String, Object>) item.get("user");
                if (user != null) {
                    Object userId = user.get("userId");
                    if (userId != null && !"undefined".equals(userId.toString())) {
                        return userId.toString();  // Safe to return
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch user mapping for orgId: {}", orgId, e);
        }

        return null;
    }
}

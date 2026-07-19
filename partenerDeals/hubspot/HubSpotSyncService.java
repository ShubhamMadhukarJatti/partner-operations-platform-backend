package com.sharkdom.service.partenerDeals.hubspot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.entity.partenearDeals.Deal;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.partnerDeals.DealRepository;
import com.sharkdom.service.partenerDeals.hubspot.dto.CreateDealRequest;
import com.sharkdom.service.partenerDeals.hubspot.dto.HubSpotDealPropertyRequest;
import com.sharkdom.service.partenerDeals.hubspot.dto.TokenResponse;
import jakarta.persistence.Column;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
public class HubSpotSyncService {

    private static final String HUBSPOT_CREATE_DEAL_URL = "https://api.hubapi.com/crm/v3/objects/deals";
    private static final String HUBSPOT_URL = "https://api.hubapi.com/crm/v3/objects/deals";

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private IntegrationRepository integrationRepository;

    @Autowired
    private HubSpotAuthService hubSpotAuthService;

    public String createDeal(CreateDealRequest dealRequest, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        // Request entity
        HttpEntity<CreateDealRequest> requestEntity = new HttpEntity<>(dealRequest, headers);

        // Send request
        ResponseEntity<String> response = restTemplate.postForEntity(
                HUBSPOT_CREATE_DEAL_URL,
                requestEntity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
            return response.getBody(); // JSON response from HubSpot
        } else {
            throw new RuntimeException("Failed to create HubSpot deal: " + response.getStatusCode());
        }
    }

    public List<Map<String, Object>> fetchDeals(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                HUBSPOT_URL,
                HttpMethod.GET,
                entity,
                Map.class
        );

        List<Map<String, Object>> filteredDeals = new ArrayList<>();

        if (response.getStatusCode().is2xxSuccessful()) {
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody().get("results");

            for (Map<String, Object> deal : results) {
                Map<String, Object> properties = (Map<String, Object>) deal.get("properties");

                Map<String, Object> dealData = new HashMap<>();
                dealData.put("id", deal.get("id"));
                dealData.put("dealname", properties.get("dealname"));
                dealData.put("dealstage", properties.get("dealstage"));
                dealData.put("hs_lastmodifieddate", properties.get("hs_lastmodifieddate"));
                dealData.put("amount", properties.get("amount"));

                filteredDeals.add(dealData);
            }
        }

        return filteredDeals;
    }

    public String fetchDealByIdWithHistory( String dealId, boolean isVendor) {
        String accessToken=null;
        Optional<Deal> optionalDeal = dealRepository.findByHotspotDealId(dealId);
        if (!optionalDeal.isPresent()) {
            throw new RuntimeException("Deal not found with ID: " + dealId);
        }

        Deal foundDeal = optionalDeal.get();
        Long vendorOrgId = foundDeal.getVendorOrgId();
        Long dealerOrgId = foundDeal.getDealerOrgId();
        IntegrationDetails optIntegrationDeatil = integrationRepository.findByOrganizationIdAndIntegrationType(vendorOrgId, IntegrationType.HUBSPOT);
        String refreshToken = optIntegrationDeatil.getRefreshToken();
        if (refreshToken != null) {
            TokenResponse tokenResponse = hubSpotAuthService.getAccessTokenUsingRefreshToken(refreshToken);
            accessToken=tokenResponse.getAccessToken();
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = HUBSPOT_URL + "/" + dealId;
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("propertiesWithHistory", "dealstage,amount,closedate");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode rootNode = (ObjectNode) objectMapper.readTree(response.getBody());

            JsonNode propertiesWithHistoryNode = rootNode.get("propertiesWithHistory");
            if (propertiesWithHistoryNode != null && propertiesWithHistoryNode.has("dealstage")) {
                ArrayNode dealstageArray = (ArrayNode) propertiesWithHistoryNode.get("dealstage");

                for (JsonNode stageNode : dealstageArray) {
                    String value = stageNode.get("value").asText();
                    String readable = DealStageReadableMessage.getReadableMessage(value, isVendor);

                    ((ObjectNode) stageNode).put("readableMessage", readable);
                }
            }
            rootNode.put("dealerOrgId", dealerOrgId);
            rootNode.put("vendorOrgId", vendorOrgId);
            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching deal details: " + e.getMessage(), e);
        }
    }

    public String createDealProperty(HubSpotDealPropertyRequest request, String accessToken) {

        String url = "https://api.hubapi.com/crm/v3/properties/deals";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<HubSpotDealPropertyRequest> entity =
                new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.CREATED
                || response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }

        throw new RuntimeException(
                "Failed to create deal property in HubSpot. Status: "
                        + response.getStatusCode() + ", Body: " + response.getBody()
        );
    }
}

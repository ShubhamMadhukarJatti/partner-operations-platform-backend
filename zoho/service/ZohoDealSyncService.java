package com.sharkdom.zoho.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.constants.partnerDeals.DealStage;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.entity.partenearDeals.Deal;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.partnerDeals.DealRepository;
import com.sharkdom.salesforce.dto.DealResponse;
import com.sharkdom.salesforce.dto.SalesforceTokenResponse;
import com.sharkdom.service.ai.ZohoService;
import com.sharkdom.zoho.dto.ZohoDealSummaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class ZohoDealSyncService {

    @Autowired
    private ZohoService zohoService;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private IntegrationRepository integrationRepository;

    @Autowired
    private ZohoAuthService zohoAuthService;

    private static final String ZOHO_DEALS_URL = "https://www.zohoapis.com/crm/v8/Deals";

    public String createDeal(String authToken, Deal deal) {
        log.info("Creating deal in Zoho for customer: {}", deal.getCustomerAccountName());
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);

        // Prepare deal data
        Map<String, Object> dealData = new HashMap<>();
        dealData.put("Deal_Name", deal.getCustomerAccountName());
        dealData.put("Stage", "Qualification");
        dealData.put("Amount", 0);
        dealData.put("Closing_Date", "2025-09-30");
        dealData.put("Pipeline", "Standard");

        Map<String, Object> body = new HashMap<>();
        body.put("data", Collections.singletonList(dealData));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(ZOHO_DEALS_URL, requestEntity, String.class);

        // Parse the response JSON
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode dataNode = root.path("data").get(0);
            String id = dataNode.path("details").path("id").asText();

            log.info("Created deal with ID: {}", id);
            return id;
        } catch (Exception e) {
            log.error("Failed to parse deal creation response: {}", e.getMessage(), e);
            return null;
        }
    }

    public ZohoDealSummaryDTO getDealSummary(String dealId, String accessToken) {
        log.info("Fetching summary for Zoho Deal ID: {}", dealId);

        String url = "https://www.zohoapis.com/crm/v8/Deals/" + dealId;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            Map<String, Object> responseBody = responseEntity.getBody();

            if (responseBody == null || !responseBody.containsKey("data")) {
                log.warn("No 'data' field found in response for Deal ID: {}", dealId);
                throw new RuntimeException("Invalid response structure for deal: " + dealId);
            }

            List<Map<String, Object>> dataList = (List<Map<String, Object>>) responseBody.get("data");
            if (dataList.isEmpty()) {
                log.warn("Empty deal data returned from Zoho for Deal ID: {}", dealId);
                throw new RuntimeException("No deal data found for ID: " + dealId);
            }

            Map<String, Object> dealData = dataList.get(0);

            String modifiedTime = dealData.get("Modified_Time") != null
                    ? dealData.get("Modified_Time").toString()
                    : null;

            Double amount = null;
            if (dealData.get("Amount") instanceof Number) {
                amount = ((Number) dealData.get("Amount")).doubleValue();
            } else {
                log.info("Amount field is null or invalid type for Deal ID: {}", dealId);
            }

            String stage = dealData.get("Stage") != null
                    ? dealData.get("Stage").toString()
                    : null;

            ZohoDealSummaryDTO summary = new ZohoDealSummaryDTO(modifiedTime, amount, stage);
            log.debug("Fetched deal summary from Zoho: {}", summary);

            return summary;

        } catch (Exception e) {
            log.error("Error fetching Zoho deal summary for ID {}: {}", dealId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch deal summary for ID: " + dealId, e);
        }
    }


    public Map<String, Object> getDealTimeline(String dealId, String accessToken) {
        log.info("Fetching timeline for deal ID: {}", dealId);
        String url = "https://www.zohoapis.com/crm/v8/Deals/" + dealId + "/__timeline";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        Map<String, Object> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class).getBody();

        if (response != null) {
            return response;
        } else {
            log.info("No timeline data found for deal ID: {}", dealId);
            throw new RuntimeException("Failed to fetch timeline for deal ID: " + dealId);
        }
    }

//    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // Every 6 hours
    public void syncDealsForAllAccounts() {
        log.info("Starting Zoho deal synchronization for all connected accounts...");

        List<IntegrationDetails> integrations = integrationRepository
                .findByIntegrationTypeAndIsConnectedAndRefreshTokenIsNotNull(
                        IntegrationType.ZOHO, true
                );

        log.info("Total Zoho integrations found: {}", integrations.size());

        for (IntegrationDetails integration : integrations) {
            Long organizationId = integration.getOrganizationId();
            log.info("Processing organizationId: {}", organizationId);

            try {
                // Refresh access token for current integration
                String accessToken = zohoAuthService.refreshAccessToken(integration.getRefreshToken());
                log.info("Successfully fetched access token for organizationId: {}", organizationId);

                // Fetch all deals (you can optimize this to fetch only org-specific deals)
                List<Deal> deals = dealRepository.findAll();
                log.info("Total deals fetched for synchronization: {}", deals.size());

                for (Deal deal : deals) {
                    if (deal.getZohoDealId() == null) {
                        continue; // Skip deals without Zoho mapping
                    }

                    log.debug("Fetching summary for Zoho Deal ID: {} (orgId: {})",
                            deal.getZohoDealId(), organizationId);

                    ZohoDealSummaryDTO dealSummary = getDealSummary(deal.getZohoDealId(), accessToken);

                    if (dealSummary == null) {
                        log.warn("No summary returned for Zoho Deal ID: {} (orgId: {})",
                                deal.getZohoDealId(), organizationId);
                        continue;
                    }

                    // Safely update deal details
                    if (dealSummary != null) {

                        // Handle amount
                        if (dealSummary.getAmount() != null) {
                            deal.setDealSize(dealSummary.getAmount().toString());
                        } else {
                            log.debug("Amount is null for ZohoDealId: {}", deal.getZohoDealId());
                        }

                        // Handle modified time
                        if (dealSummary.getModifiedTime() != null && !dealSummary.getModifiedTime().isEmpty()) {
                            try {
                                deal.setLastActivity(getTimeAgoWithOffset(dealSummary.getModifiedTime()));
                            } catch (Exception e) {
                                log.warn("Failed to parse modified time for ZohoDealId: {}. Value: {}. Error: {}",
                                        deal.getZohoDealId(), dealSummary.getModifiedTime(), e.getMessage());
                            }
                        } else {
                            log.debug("ModifiedTime is null or empty for ZohoDealId: {}", deal.getZohoDealId());
                        }

                        // Handle deal stage
                        if (dealSummary.getStage() != null && !dealSummary.getStage().isEmpty()) {
                            try {
                                deal.setDealStage(convertZohoValueToEnum(dealSummary.getStage()));
                            } catch (IllegalArgumentException e) {
                                log.warn("Unrecognized Zoho stage '{}' for ZohoDealId: {}. Keeping existing stage.",
                                        dealSummary.getStage(), deal.getZohoDealId());
                            }
                        } else {
                            log.debug("Stage is null or empty for ZohoDealId: {}", deal.getZohoDealId());
                        }

                    } else {
                        log.warn("Deal summary is null for ZohoDealId: {}", deal.getZohoDealId());
                    }

                    Deal updatedDeal = dealRepository.save(deal);
                    log.info("Updated Zoho deal (ID: {}, ZohoDealId: {}, Stage: {}, Amount: {})",
                            updatedDeal.getId(), updatedDeal.getZohoDealId(),
                            updatedDeal.getDealStage(), updatedDeal.getDealSize());
                }

            } catch (Exception e) {
                log.error("Error occurred while syncing deals for organizationId: {}. Message: {}",
                        organizationId, e.getMessage(), e);
            }
        }

        log.info("Zoho deal synchronization completed for all accounts.");
    }


    public Map<String, Object> getZohoDealHistory(String dealId)
    {
        log.info("Fetching history for deal ID: {}", dealId);
        Optional<Deal> optDeal = dealRepository.findByZohoDealId(dealId);
        if(optDeal.isPresent())
        {
            Deal deal = optDeal.get();
            IntegrationDetails integration = integrationRepository.findByOrganizationIdAndIntegrationType(deal.getVendorOrgId(), IntegrationType.ZOHO);
            String accessToken = zohoAuthService.refreshAccessToken(integration.getRefreshToken());
            return getDealTimeline(dealId, accessToken);
        }
        else
        {
            throw new RuntimeException("No deal found with ID: " + dealId);
        }
    }

    public static DealStage convertZohoValueToEnum(String apiValue) {
        if (apiValue == null || apiValue.isEmpty()) {
            throw new IllegalArgumentException("Zoho API value cannot be null or empty");
        }

        log.info("Converting Zoho API value to enum: {}", apiValue);

        // Normalize value: lowercase, trim spaces, replace slashes and underscores
        String normalized = apiValue.trim().toLowerCase().replace("/", "_").replace(" ", "_");

        return switch (normalized) {
            // Salesforce & Zoho equivalent stages
            case "qualification" -> DealStage.QUALIFICATION;
            case "needs_analysis" -> DealStage.NEEDS_ANALYSIS;
            case "value_proposition" -> DealStage.VALUE_PROPOSITION;
            case "identify_decision_makers" -> DealStage.IDENTIFY_DECISION_MAKERS;
            case "proposal_price_quote" -> DealStage.PROPOSAL_PRICE_QUOTE;
            case "negotiation_review" -> DealStage.NEGOTIATION_REVIEW;
            case "proposal" -> DealStage.PROPOSAL;
            case "negotiation" -> DealStage.NEGOTIATION;
            case "closed_won" -> DealStage.CLOSED_WON;
            case "closed_lost" -> DealStage.CLOSED_LOST;
            case "closed" -> DealStage.CLOSED;
            default -> {
                log.warn("Unknown Zoho deal stage encountered: {}", apiValue);
                throw new IllegalArgumentException("Unknown Zoho deal stage: " + apiValue);
            }
        };
    }


    public static String getTimeAgoWithOffset(String timestampWithOffset) {
        // Parse the timestamp with offset
        OffsetDateTime lastModifiedOffset = OffsetDateTime.parse(timestampWithOffset, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        // Convert to UTC ZonedDateTime
        ZonedDateTime lastModified = lastModifiedOffset.atZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        Duration duration = Duration.between(lastModified, now);

        if (duration.toMinutes() < 1) {
            return "Just now";
        } else if (duration.toMinutes() < 60) {
            return duration.toMinutes() + " minutes ago";
        } else if (duration.toHours() < 24) {
            return duration.toHours() + " hours ago";
        } else if (duration.toDays() < 7) {
            return duration.toDays() + " days ago";
        } else {
            return "on " + lastModified.toLocalDate();
        }
    }
}

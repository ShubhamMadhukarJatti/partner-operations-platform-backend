package com.sharkdom.salesforce.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class SalesforceSyncService {

    @Autowired
    private IntegrationRepository integrationRepository;

    @Autowired
    private SalesforceAuthService salesforceAuthService;

    @Autowired
    private DealRepository dealRepository;

    public String createDeal(String baseUrl,
                             String accessToken,
                             Deal deal) throws JsonProcessingException {
        log.info("SalesforceSyncService createDeal");

        // Salesforce Opportunity endpoint
        String url = baseUrl + "/services/data/v61.0/sobjects/Opportunity";

        // Build the Opportunity request body dynamically
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("Name", deal.getCustomerAccountName());
        requestBody.put("StageName", "Prospecting");
        requestBody.put("CloseDate", "2025-09-30");
        requestBody.put("Amount", 0);
        requestBody.put("Type", "newbusiness");

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        // Build the HTTP request
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Call Salesforce API
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        // Extract ID only
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(response.getBody());
        return root.path("id").asText();
    }

//    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // Every 6 hours
    public void syncDealsForAllAccounts() {
        log.info("Starting scheduled job to sync Salesforce deals for all connected accounts");

        List<IntegrationDetails> integrations = integrationRepository
            .findByIntegrationTypeAndIsConnectedAndRefreshTokenIsNotNull(
                    IntegrationType.SALESFORCE, true
            );

    for (IntegrationDetails integration : integrations) {
        try {
            SalesforceTokenResponse salesforceTokenResponse =
                    salesforceAuthService.refreshAccessToken(integration.getRefreshToken());
            log.info("Refreshed Salesforce access token for orgId: {}", integration.getOrganizationId());

            String accessToken = salesforceTokenResponse.accessToken();
            String instanceUrl = salesforceTokenResponse.instanceUrl();

            log.info("Syncing Salesforce deals for orgId: {}", integration.getOrganizationId());
            List<Deal> deals = dealRepository.findAll();

            for (Deal deal : deals) {
                if (deal.getSalesforceDealId() == null) continue;

                DealResponse dealResponse = getOpportunityById(instanceUrl, deal.getSalesforceDealId(), accessToken);
                log.info("Fetched Salesforce deal: {}", dealResponse);

                // Only update non-null values
                if (dealResponse.getAmount() != null) {
                    deal.setDealSize(dealResponse.getAmount().toString());
                }

                if (dealResponse.getLastModifiedDate() != null) {
                    String timeAgo = getTimeAgoSafe(dealResponse.getLastModifiedDate());
                    if (timeAgo != null) {
                        log.info("Calculated time ago for last modified date: {}", timeAgo);
                        deal.setLastActivity(timeAgo);
                    }
                }

                if (dealResponse.getStageName() != null) {
                    DealStage stage = convertSalesforceValueToEnumSafe(dealResponse.getStageName());
                    log.info("Converted Salesforce stage '{}' to enum: {}", dealResponse.getStageName(), stage);
                    if (stage != null) {
                        deal.setDealStage(stage);
                    }
                }

                Deal updatedDeal = dealRepository.save(deal);
                log.info("Updated Salesforce deal: {}", updatedDeal);
            }

        } catch (Exception e) {
            System.err.println("Error syncing deals for orgId: " + integration.getOrganizationId() + " - " + e.getMessage());
        }
    }
}


    public DealResponse getOpportunityById(String instanceUrl, String opportunityId, String accessToken) {
        log.info("SalesforceSyncService getOpportunityById");
        String url = instanceUrl + "/services/data/v61.0/sobjects/Opportunity/" + opportunityId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<DealResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                DealResponse.class
        );

        return response.getBody();
    }


    public String getTimeAgoSafe(String utcTimestamp) {
        try {
            log.info("Calculating time ago for timestamp: {}", utcTimestamp);

            // Normalize timezone format: +0000 → +00:00
            String normalized = utcTimestamp.replaceFirst("(\\+|\\-)(\\d{2})(\\d{2})$", "$1$2:$3");

            ZonedDateTime lastModified = ZonedDateTime.parse(normalized, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            Duration duration = Duration.between(lastModified, now);

            if (duration.toMinutes() < 1) return "Just now";
            else if (duration.toMinutes() < 60) return duration.toMinutes() + " minutes ago";
            else if (duration.toHours() < 24) return duration.toHours() + " hours ago";
            else if (duration.toDays() < 7) return duration.toDays() + " days ago";
            else return "on " + lastModified.toLocalDate();

        } catch (Exception e) {
            log.error("Error parsing timestamp: {}", utcTimestamp, e);
            return null; // return null to skip assignment safely
        }
    }


    public DealStage convertSalesforceValueToEnumSafe(String apiValue) {
        try {
            if (apiValue == null || apiValue.isEmpty()) {
                return null;
            }

            log.info("Converting Salesforce API value to enum: {}", apiValue);

            return switch (apiValue.trim().toLowerCase()) {
                case "qualification" -> DealStage.QUALIFICATION;
                case "needs analysis" -> DealStage.NEEDS_ANALYSIS;
                case "proposal" -> DealStage.PROPOSAL;
                case "negotiation" -> DealStage.NEGOTIATION;
                case "closed won" -> DealStage.CLOSED_WON;
                case "closed lost" -> DealStage.CLOSED_LOST;
                default -> null; // gracefully handle unknown stage
            };
        } catch (Exception e) {
            log.error("Error converting Salesforce stage value: {}", apiValue, e);
            return null;
        }
    }


    public Map<String, Object> getOpportunityHistory(String baseUrl, String opportunityId, String accessToken) {
        // Construct raw SOQL query
        String soql = "SELECT OpportunityId, StageName, CloseDate, Amount, Probability, CreatedDate "
                + "FROM OpportunityHistory "
                + "WHERE OpportunityId = '" + opportunityId + "' "
                + "ORDER BY CreatedDate DESC";

        // Append query without encoding (Salesforce handles this fine)
        String url = baseUrl + "/services/data/v61.0/query/?q=" + soql;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            Map<String, Object> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class).getBody();
            if (response != null) return response;
            else throw new RuntimeException("Empty response from Salesforce");
        } catch (Exception e) {
            log.error("Error fetching OpportunityHistory: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch OpportunityHistory for ID: " + opportunityId);
        }
    }

    public Map<String,Object> getSalesForceDealHistory(String dealId)
    {
        log.info("SalesforceSyncService getSalesForceDealHistory");
        Optional<Deal> optDeal = dealRepository.findBySalesforceDealId(dealId);
        if (optDeal.isPresent()) {
            Deal deal = optDeal.get();
            IntegrationDetails integration = integrationRepository.findByOrganizationIdAndIntegrationType(deal.getVendorOrgId(), IntegrationType.SALESFORCE);
            SalesforceTokenResponse salesforceTokenResponse = salesforceAuthService.refreshAccessToken(integration.getRefreshToken());
            return getOpportunityHistory(salesforceTokenResponse.instanceUrl(), deal.getSalesforceDealId(), salesforceTokenResponse.accessToken());
        }
        else {
            throw new RuntimeException("No deal found with ID: " + dealId);
        }
    }
}

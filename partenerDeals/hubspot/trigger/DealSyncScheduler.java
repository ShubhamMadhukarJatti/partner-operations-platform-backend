package com.sharkdom.service.partenerDeals.hubspot.trigger;

import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.constants.partnerDeals.DealStage;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.entity.partenearDeals.Deal;
import com.sharkdom.repository.campaign.GeneralTriggerRepository;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.partnerDeals.DealRepository;
import com.sharkdom.reseller.entity.ResellerDealDetails;
import com.sharkdom.reseller.entity.ResellerDealStag;
import com.sharkdom.reseller.repository.ResellerDealDetailsRepository;
import com.sharkdom.service.partenerDeals.hubspot.HubSpotAuthService;
import com.sharkdom.service.partenerDeals.hubspot.HubSpotSyncService;
import com.sharkdom.service.partenerDeals.hubspot.dto.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class DealSyncScheduler {

    @Autowired
    private HubSpotSyncService hubSpotSyncService;

    @Autowired
    private HubSpotAuthService hubSpotAuthService;

    @Autowired
    private IntegrationRepository integrationRepository;

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private ResellerDealDetailsRepository resellerDealDetailsRepository;

//    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void syncDealsForAllAccounts() {
        List<IntegrationDetails> integrations = integrationRepository
                .findByIntegrationTypeAndIsConnectedAndRefreshTokenIsNotNull(
                        IntegrationType.HUBSPOT, true
                );

        for (IntegrationDetails integration : integrations) {
            try {

                TokenResponse tokens = hubSpotAuthService.getAccessTokenUsingRefreshToken(
                        integration.getRefreshToken()
                );

                // Update the new refresh token in DB
                integration.setRefreshToken(tokens.getRefreshToken());
                integrationRepository.save(integration); // Save immediately

                String accessToken = tokens.getAccessToken();

                List<Map<String, Object>> hubspotDeals = hubSpotSyncService.fetchDeals(accessToken);

                for (Map<String, Object> hubspotDeal : hubspotDeals) {
                    String hubspotId = (String) hubspotDeal.get("id");
                    Optional<Deal> optionalDeal = dealRepository.findByHotspotDealId(hubspotId);
                    if (optionalDeal.isPresent()) {
                        Deal deal = optionalDeal.get();
                        deal.setDealSize((String) hubspotDeal.get("amount"));
                        log.info("dealamount: " + (String) hubspotDeal.get("amount"));
                        deal.setDealStage(convertApiValueToEnum((String) hubspotDeal.get("dealstage")));
                        log.info("dealstage: " + (String) hubspotDeal.get("dealstage"));
                        deal.setLastUpdatedTimestamp(convertToDateTime(hubspotDeal.get("hs_lastmodifieddate")));
                        log.info("lastUpdatedTimestamp: " + convertToDateTime(hubspotDeal.get("hs_lastmodifieddate")));
                        deal.setLastActivity(getTimeAgo(hubspotDeal.get("hs_lastmodifieddate").toString()));
                        log.info("lastActivity: " + getTimeAgo(hubspotDeal.get("hs_lastmodifieddate").toString()));
                        Deal updatedDeal = dealRepository.save(deal);
                        log.info("Updated existing deal: {}", updatedDeal);
                    }
                }
            } catch (Exception e) {
                // Log error with organizationId for traceability
                System.err.println("Error syncing deals for orgId: " + integration.getOrganizationId() + " - " + e.getMessage());
            }
        }
    }

    private Date convertToDateTime(Object isoDate) {
        try {
            return Date.from(Instant.parse((String) isoDate));
        } catch (Exception e) {
            return null;
        }
    }

    public static DealStage convertApiValueToEnum(String apiValue) {
        if (apiValue == null || apiValue.isEmpty()) {
            throw new IllegalArgumentException("API value cannot be null or empty");
        }
        log.info("Converting API value to enum: {}", apiValue);
        // Convert the API value to lowercase and match it with the enum values
        return switch (apiValue.toLowerCase()) {
            case "appointmentscheduled" -> DealStage.APPOINTMENT_SCHEDULED;
            case "qualifiedtobuy" -> DealStage.QUALIFIED_TO_BUY;
            case "presentationscheduled" -> DealStage.PRESENTATION_SCHEDULED;
            case "decisionmakerboughtin" -> DealStage.DECISION_MAKER_BOUGHT_IN;
            case "contractsent" -> DealStage.CONTRACT_SENT;
            case "closedwon" -> DealStage.CLOSED_WON;
            case "closedlost" -> DealStage.CLOSED_LOST;
            default -> throw new IllegalArgumentException("Unknown API deal stage: " + apiValue);
        };
    }

    public static ResellerDealStag convertApiValueToEnumForReseller(String apiValue) {
        if (apiValue == null || apiValue.isEmpty()) {
            throw new IllegalArgumentException("API value cannot be null or empty");
        }
        log.info("Converting API value to enum: {}", apiValue);
        // Convert the API value to lowercase and match it with the enum values
        return switch (apiValue.toLowerCase()) {
            case "appointmentscheduled" -> ResellerDealStag.APPOINTMENT_SCHEDULED;
            case "qualifiedtobuy" -> ResellerDealStag.QUALIFIED_TO_BUY;
            case "presentationscheduled" -> ResellerDealStag.PRESENTATION_SCHEDULED;
            case "decisionmakerboughtin" -> ResellerDealStag.DECISION_MAKER_BOUGHT_IN;
            case "contractsent" -> ResellerDealStag.CONTRACT_SENT;
            case "closedwon" -> ResellerDealStag.CLOSED_WON;
            case "closedlost" -> ResellerDealStag.CLOSED_LOST;
            default -> throw new IllegalArgumentException("Unknown API deal stage: " + apiValue);
        };
    }



    public String getTimeAgo(String utcTimestamp) {

        ZonedDateTime lastModified = ZonedDateTime.parse(utcTimestamp, DateTimeFormatter.ISO_DATE_TIME);


        ZonedDateTime now = ZonedDateTime.now(java.time.ZoneOffset.UTC);

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

    private Double parseDouble(Object value) {
        if (value == null) return null;
        try {
            return Double.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

//    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void syncDealsForAllResellerDealsAccounts() {
        List<IntegrationDetails> integrations = integrationRepository
                .findByIntegrationTypeAndIsConnectedAndRefreshTokenIsNotNull(
                        IntegrationType.HUBSPOT, true
                );

        for (IntegrationDetails integration : integrations) {
            try {

                TokenResponse tokens = hubSpotAuthService.getAccessTokenUsingRefreshToken(
                        integration.getRefreshToken()
                );

                // Update the new refresh token in DB
                integration.setRefreshToken(tokens.getRefreshToken());
                integrationRepository.save(integration); // Save immediately

                String accessToken = tokens.getAccessToken();

                List<Map<String, Object>> hubspotDeals = hubSpotSyncService.fetchDeals(accessToken);

                for (Map<String, Object> hubspotDeal : hubspotDeals) {
                    String hubspotId = (String) hubspotDeal.get("id");
                    Optional<ResellerDealDetails> optionalDeal = resellerDealDetailsRepository.findByHubspotDealId(hubspotId);
                    if (optionalDeal.isPresent()) {
                        ResellerDealDetails deal = optionalDeal.get();
                        deal.setBuyPrice(parseDouble(hubspotDeal.get("buy_price")));
                        deal.setActualPrice(parseDouble(hubspotDeal.get("actual_price")));
                        deal.setResellerDealStag(convertApiValueToEnumForReseller((String) hubspotDeal.get("dealstage")));
                        log.info("dealstage: " + (String) hubspotDeal.get("dealstage"));
                        deal.setLastUpdatedTimestamp(convertToDateTime(hubspotDeal.get("hs_lastmodifieddate")));
                        log.info("lastUpdatedTimestamp: " + convertToDateTime(hubspotDeal.get("hs_lastmodifieddate")));
                        deal.setLastActivity(getTimeAgo(hubspotDeal.get("hs_lastmodifieddate").toString()));
                        log.info("lastActivity: " + getTimeAgo(hubspotDeal.get("hs_lastmodifieddate").toString()));
                        ResellerDealDetails updatedDeal = resellerDealDetailsRepository.save(deal);
                        log.info("Updated existing deal: {}", updatedDeal);
                    }
                }
            } catch (Exception e) {
                // Log error with organizationId for traceability
                System.err.println("Error syncing deals for orgId: " + integration.getOrganizationId() + " - " + e.getMessage());
            }
        }
    }

}

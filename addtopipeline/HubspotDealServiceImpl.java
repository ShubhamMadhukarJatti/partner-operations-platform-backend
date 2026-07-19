package com.sharkdom.partnerattribution.addtopipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.service.partenerDeals.hubspot.HubSpotAuthService;
import com.sharkdom.service.partenerDeals.hubspot.HubSpotSyncService;
import com.sharkdom.service.partenerDeals.hubspot.dto.CreateDealRequest;
import com.sharkdom.service.partenerDeals.hubspot.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HubspotDealServiceImpl implements HubspotDealService {

    private final OrganizationRepository organizationRepository;
    private final IntegrationRepository integrationRepository;
    private final HubSpotAuthService hubSpotAuthService;
    private final HubSpotSyncService hubSpotSyncService;

    @Override
    public CreateHubspotDealResponseDto createDealHubspot(
            CreateHubspotDealRequestDto requestDto) {

        try {

            Organization organization = organizationRepository
                    .findById(requestDto.getOrgId())
                    .orElseThrow(() -> new ServiceException(
                            ErrorMessages.SH22,
                            requestDto.getOrgId()));

            IntegrationDetails integrationDetails =
                    integrationRepository
                            .findByOrganizationIdAndIntegrationType(
                                    requestDto.getOrgId(),
                                    IntegrationType.HUBSPOT);

            if (integrationDetails == null) {
                throw new ServiceException(
                        ErrorMessages.SH126);
            }

            String refreshToken =
                    integrationDetails.getRefreshToken();

            log.info("Using refresh token for HubSpot");

            TokenResponse tokenResponse =
                    hubSpotAuthService
                            .getAccessTokenUsingRefreshToken(
                                    refreshToken);

            String accessToken =
                    tokenResponse.getAccessToken();

            log.info("HubSpot access token generated");

            CreateDealRequest createDealRequest =
                    new CreateDealRequest();

            createDealRequest.setProperties(
                    requestDto.getProperties());

            String hubspotResponse =
                    hubSpotSyncService.createDeal(
                            createDealRequest,
                            accessToken);

            log.info("Deal created successfully on HubSpot");

            ObjectMapper objectMapper =
                    new ObjectMapper();

            JsonNode root =
                    objectMapper.readTree(
                            hubspotResponse);

            String hubspotDealId =
                    root.path("id").asText();

            return CreateHubspotDealResponseDto
                    .builder()
                    .hubspotDealId(hubspotDealId)
                    .response(hubspotResponse)
                    .message("Deal created successfully")
                    .build();

        } catch (Exception ex) {

            log.error("Error while creating HubSpot deal",
                    ex);

            throw new ServiceException(
                    ErrorMessages.SH116,
                    ex.getMessage());
        }
    }
}
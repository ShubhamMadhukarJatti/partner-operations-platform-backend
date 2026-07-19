package com.sharkdom.partnerattribution.hubspot;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.partnerattribution.dto.HubSpotOwnerResponseDto;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.service.partenerDeals.hubspot.HubSpotAuthService;
import com.sharkdom.service.partenerDeals.hubspot.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubSpotOwnerService {

    private final IntegrationRepository integrationRepository;
    private final HubSpotAuthService hubSpotAuthService;

    private final RestTemplate restTemplate;

    @Transactional(readOnly = true)
    public HubSpotOwnerResponseDto getOwnerById(
            Long organizationId,
            String ownerId
    ) {

        log.info(
                "Started fetching HubSpot owner details. organizationId: {}, ownerId: {}",
                organizationId,
                ownerId
        );

        validateRequest(
                organizationId,
                ownerId
        );

        IntegrationDetails integration =
                fetchHubSpotIntegration(organizationId);


        var accessTokenUsingRefreshToken = hubSpotAuthService.getAccessTokenUsingRefreshToken(
                integration.getRefreshToken()
        );

        HubSpotOwnerResponseDto responseDto =
                fetchOwnerDataFromHubSpot(
                        accessTokenUsingRefreshToken.getAccessToken(),
                        ownerId
                );

        log.info(
                "Successfully fetched HubSpot owner details. organizationId: {}, ownerId: {}, email: {}",
                organizationId,
                ownerId,
                responseDto.getEmail()
        );

        return responseDto;
    }

    private void validateRequest(
            Long organizationId,
            String ownerId
    ) {

        log.debug(
                "Validating HubSpot owner request. organizationId: {}, ownerId: {}",
                organizationId,
                ownerId
        );

        if (organizationId == null) {

            log.error(
                    "OrganizationId is null while fetching HubSpot owner"
            );

            throw new SharkdomException(
                    ErrorMessages.SH199
            );
        }

        if (!StringUtils.hasText(ownerId)) {

            log.error(
                    "OwnerId is null or empty while fetching HubSpot owner. organizationId: {}",
                    organizationId
            );

            throw new SharkdomException(
                    ErrorMessages.SH106
            );
        }
    }

    private IntegrationDetails fetchHubSpotIntegration(
            Long organizationId
    ) {

        log.info(
                "Fetching HubSpot integration details for organizationId: {}",
                organizationId
        );

        IntegrationDetails integration =
                integrationRepository
                        .findByOrganizationIdAndIntegrationType(
                                organizationId,
                                IntegrationType.HUBSPOT
                        );

        if (integration == null) {

            log.error(
                    "HubSpot integration not found for organizationId: {}",
                    organizationId
            );

            throw new SharkdomException(
                    ErrorMessages.SH21
            );
        }

        if (!StringUtils.hasText(integration.getRefreshToken())) {

            log.error(
                    "Refresh token missing for HubSpot integration. organizationId: {}",
                    organizationId
            );

            throw new SharkdomException(
                    ErrorMessages.SH10
            );
        }

        log.info(
                "Successfully fetched HubSpot integration details for organizationId: {}",
                organizationId
        );

        return integration;
    }

    private HubSpotOwnerResponseDto fetchOwnerDataFromHubSpot(
            String accessToken,
            String ownerId
    ) {

        String url =
                "https://api.hubapi.com/crm/v3/owners/" + ownerId;

        log.info(
                "Calling HubSpot Owner API. url: {}, ownerId: {}",
                url,
                ownerId
        );

        try {

            HttpHeaders headers = new HttpHeaders();

            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_JSON_VALUE));

            HttpEntity<Void> requestEntity =
                    new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            requestEntity,
                            new ParameterizedTypeReference<>() {}
                    );

            log.info(
                    "Received response from HubSpot Owner API. statusCode: {}",
                    response.getStatusCode()
            );

            Map<String, Object> responseBody =
                    response.getBody();

            if (Objects.isNull(responseBody)) {

                log.error(
                        "HubSpot Owner API returned null response body for ownerId: {}",
                        ownerId
                );

                throw new SharkdomException(
                        ErrorMessages.SH160,
                        "HubSpot response body is null"
                );
            }

            log.debug(
                    "HubSpot Owner API raw response: {}",
                    responseBody
            );

            HubSpotOwnerResponseDto dto =
                    mapToOwnerResponseDto(responseBody);

            log.info(
                    "Successfully mapped HubSpot owner response for ownerId: {}",
                    ownerId
            );

            return dto;

        } catch (HttpClientErrorException exception) {

            log.error(
                    "HubSpot client error while fetching owner. statusCode: {}, response: {}",
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString(),
                    exception
            );

            throw new SharkdomException(
                    ErrorMessages.SH116,
                    exception.getResponseBodyAsString()
            );

        } catch (HttpServerErrorException exception) {

            log.error(
                    "HubSpot server error while fetching owner. statusCode: {}, response: {}",
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString(),
                    exception
            );

            throw new SharkdomException(
                    ErrorMessages.SH116,
                    exception.getResponseBodyAsString()
            );

        } catch (SharkdomException exception) {

            log.error(
                    "SharkdomException occurred while fetching HubSpot owner. message: {}",
                    exception.getMessage(),
                    exception
            );

            throw exception;

        } catch (Exception exception) {

            log.error(
                    "Unexpected error occurred while fetching HubSpot owner by id: {}",
                    ownerId,
                    exception
            );

            throw new SharkdomException(
                    ErrorMessages.SH160,
                    exception.getMessage()
            );
        }
    }

    private HubSpotOwnerResponseDto mapToOwnerResponseDto(
            Map<String, Object> response
    ) {

        log.debug(
                "Started mapping HubSpot response to DTO"
        );

        String email =
                response.get("email") != null
                        ? response.get("email").toString()
                        : null;

        String firstName =
                response.get("firstName") != null
                        ? response.get("firstName").toString()
                        : null;

        String lastName =
                response.get("lastName") != null
                        ? response.get("lastName").toString()
                        : null;

        if (!StringUtils.hasText(email)) {

            log.error(
                    "Email not found in HubSpot owner response"
            );

            throw new SharkdomException(
                    ErrorMessages.SH117
            );
        }

        HubSpotOwnerResponseDto dto =
                HubSpotOwnerResponseDto
                        .builder()
                        .email(email)
                        .firstName(firstName)
                        .lastName(lastName)
                        .build();

        log.debug(
                "Successfully mapped HubSpot response DTO: {}",
                dto
        );

        return dto;
    }
}
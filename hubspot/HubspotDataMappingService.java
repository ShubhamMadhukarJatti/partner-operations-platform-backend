package com.sharkdom.partnerattribution.hubspot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.model.ai.HubspotTokenResponse;
import com.sharkdom.partnerattribution.dto.*;
import com.sharkdom.repository.organization.IntegrationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubspotDataMappingService {

    private final RestTemplate restTemplate;
    private final IntegrationRepository integrationRepository;

    @Value("${hubspot.id}")
    private String hubspotId;

    @Value("${hubspot.secret}")
    private String hubspotSecret;

    @Value("${env}")
    private String env;

    private String getAccessToken(Long organizationId) {

        var integration = integrationRepository
                .findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.HUBSPOT
                );

        if (integration == null) {
            log.error(
                    "HubSpot integration not found for organizationId: {}",
                    organizationId
            );
            throw new SharkdomException(ErrorMessages.SH91);
        }

        if (integration.getRefreshToken() == null) {
            log.error(
                    "HubSpot refresh token not found for organizationId: {}",
                    organizationId
            );
            throw new SharkdomException(ErrorMessages.SH91);
        }

        return generateAccessToken(
                integration.getRefreshToken()
        );
    }

    private String generateAccessToken(
            String refreshToken
    ) {

        log.info("Generating HubSpot access token");

        String url =
                "https://api.hubapi.com/oauth/v1/token";

        try {

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setContentType(
                    MediaType.APPLICATION_FORM_URLENCODED
            );

            MultiValueMap<String, String> body =
                    new LinkedMultiValueMap<>();

            body.add(
                    "grant_type",
                    "refresh_token"
            );
            body.add(
                    "client_id",
                    hubspotId
            );
            body.add(
                    "client_secret",
                    hubspotSecret
            );
            body.add(
                    "refresh_token",
                    refreshToken
            );

            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(
                            body,
                            headers
                    );

            ResponseEntity<HubspotTokenResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            request,
                            HubspotTokenResponse.class
                    );

            HubspotTokenResponse tokenResponse =
                    response.getBody();

            if (tokenResponse == null
                    || tokenResponse.getAccess_token() == null) {

                log.error(
                        "HubSpot access token generation failed"
                );

                throw new SharkdomException(
                        ErrorMessages.SH91
                );
            }

            log.info(
                    "HubSpot access token generated successfully"
            );

            return tokenResponse.getAccess_token();

        } catch (Exception e) {

            log.error(
                    "Error while generating HubSpot access token",
                    e
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );
        }
    }

    @Transactional
    public DealCompanyAssociationResponse getDealCompanyAssociation(
            Long organizationId,
            String dealId
    ) {

        log.info(
                "Fetching company association for dealId: {} organizationId: {}",
                dealId,
                organizationId
        );

        String accessToken =
                getAccessToken(
                        organizationId
                );

        return getDealCompanyAssociationsData(
                accessToken,
                dealId
        );
    }

    private DealCompanyAssociationResponse getDealCompanyAssociationsData(
            String accessToken,
            String dealId
    ) {

        String url =
                "https://api.hubapi.com/crm/v4/objects/deals/"
                        + dealId
                        + "/associations/companies";

        log.info(
                "Calling HubSpot Deal -> Company Associations API: {}",
                url
        );

        try {

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setBearerAuth(
                    accessToken
            );

            headers.setAccept(
                    List.of(MediaType.APPLICATION_JSON)
            );

            headers.set(
                    HttpHeaders.ACCEPT_ENCODING,
                    "identity"
            );

            HttpEntity<String> requestEntity =
                    new HttpEntity<>(
                            headers
                    );

            ParameterizedTypeReference<Map<String, Object>> responseType =
                    new ParameterizedTypeReference<>() {};

            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            requestEntity,
                            responseType
                    );

            log.info(
                    "HubSpot association response status: {}",
                    response.getStatusCode()
            );

            Map<String, Object> responseBody =
                    response.getBody();

            log.info(
                    "HubSpot association response body: {}",
                    responseBody
            );

            if (responseBody == null
                    || !responseBody.containsKey("results")) {

                log.error(
                        "No results found in HubSpot response for dealId: {}",
                        dealId
                );

                throw new SharkdomException(
                        ErrorMessages.SH91
                );
            }

            List<?> results =
                    (List<?>) responseBody.get(
                            "results"
                    );

            if (results == null
                    || results.isEmpty()) {

                log.error(
                        "Empty company associations found for dealId: {}",
                        dealId
                );

                throw new SharkdomException(
                        ErrorMessages.SH91
                );
            }

            Map<?, ?> firstResult =
                    (Map<?, ?>) results.get(0);

            Object companyIdObject =
                    firstResult.get(
                            "toObjectId"
                    );

            if (companyIdObject == null) {

                log.error(
                        "toObjectId missing in HubSpot response for dealId: {}",
                        dealId
                );

                throw new SharkdomException(
                        ErrorMessages.SH91
                );
            }

            Long companyId =
                    Long.valueOf(
                            companyIdObject.toString()
                    );

            log.info(
                    "Successfully fetched company association. dealId: {} companyId: {}",
                    dealId,
                    companyId
            );

            return new DealCompanyAssociationResponse(
                    companyId
            );

        } catch (HttpClientErrorException e) {

            log.error(
                    "HubSpot client error while fetching company association. status: {} response: {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );

        } catch (HttpServerErrorException e) {

            log.error(
                    "HubSpot server error while fetching company association. status: {} response: {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );

        } catch (RestClientException e) {

            log.error(
                    "Rest client exception while fetching company association for dealId: {}",
                    dealId,
                    e
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );

        } catch (Exception e) {

            log.error(
                    "Unexpected error while fetching company association for dealId: {}",
                    dealId,
                    e
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );
        }
    }

    @Transactional
    public DealContactAssociationResponse getDealContactAssociations(
            Long organizationId,
            String dealId
    ) {

        log.info(
                "Fetching contact association for dealId: {} organizationId: {}",
                dealId,
                organizationId
        );

        String accessToken =
                getAccessToken(
                        organizationId
                );

        return getDealContactAssociationsData(
                accessToken,
                dealId
        );
    }

    private DealContactAssociationResponse getDealContactAssociationsData(
            String accessToken,
            String dealId
    ) {

        String url =
                "https://api.hubapi.com/crm/v4/objects/deals/"
                        + dealId
                        + "/associations/contacts";

        try {

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setBearerAuth(
                    accessToken
            );

            HttpEntity<String> request =
                    new HttpEntity<>(
                            headers
                    );

            ResponseEntity<HubspotDealContactAssociationDto> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            request,
                            HubspotDealContactAssociationDto.class
                    );

            HubspotDealContactAssociationDto body =
                    response.getBody();

            if (body == null
                    || body.getResults() == null
                    || body.getResults().isEmpty()) {

                log.error(
                        "No contact association found for dealId: {}",
                        dealId
                );

                throw new SharkdomException(
                        ErrorMessages.SH91
                );
            }

            Long contactId =
                    body.getResults()
                            .get(0)
                            .getToObjectId();

            log.info(
                    "Contact association found. contactId: {}",
                    contactId
            );

            return new DealContactAssociationResponse(
                    contactId
            );

        } catch (HttpClientErrorException e) {

            log.error(
                    "HubSpot client error while fetching contact association: {}",
                    e.getResponseBodyAsString()
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );

        } catch (HttpServerErrorException e) {

            log.error(
                    "HubSpot server error while fetching contact association: {}",
                    e.getResponseBodyAsString()
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );

        } catch (Exception e) {

            log.error(
                    "Unexpected error while fetching contact association",
                    e
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );
        }
    }

    @Transactional
    public HubspotContactResponseDto getContactById(
            Long organizationId,
            String contactId,
            String properties
    ) {

        log.info(
                "Fetching contact by id: {}",
                contactId
        );

        String accessToken =
                getAccessToken(
                        organizationId
                );

        return getContactByIdData(
                accessToken,
                contactId,
                properties
        );
    }

    private HubspotContactResponseDto getContactByIdData(
            String accessToken,
            String contactId,
            String properties
    ) {

        String url =
                "https://api.hubapi.com/crm/v3/objects/contacts/"
                        + contactId
                        + "?properties="
                        + properties
                        + "&associations=deals"
                        + "&associations=companies"
                        + "&archived=false";

        try {

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setBearerAuth(
                    accessToken
            );

            headers.setAccept(
                    MediaType.parseMediaTypes(
                            MediaType.APPLICATION_JSON_VALUE
                    )
            );

            HttpEntity<String> request =
                    new HttpEntity<>(
                            headers
                    );

            ResponseEntity<HubspotContactRawResponseDto> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            request,
                            HubspotContactRawResponseDto.class
                    );

            HubspotContactRawResponseDto body =
                    response.getBody();

            if (body == null
                    || body.getProperties() == null) {

                log.error(
                        "Contact data not found for contactId: {}",
                        contactId
                );

                throw new SharkdomException(
                        ErrorMessages.SH91
                );
            }

            return mapToContactResponse(
                    body.getProperties()
            );

        } catch (Exception e) {

            log.error(
                    "Error while fetching contact by id: {}",
                    contactId,
                    e
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );
        }
    }

    @Transactional
    public HubspotCompanyResponseDto getCompanyById(
            Long organizationId,
            String companyId,
            String properties
    ) {

        log.info(
                "Fetching company by id: {}",
                companyId
        );

        String accessToken =
                getAccessToken(
                        organizationId
                );

        return getCompanyByIdData(
                accessToken,
                companyId,
                properties
        );
    }

    private HubspotCompanyResponseDto getCompanyByIdData(
            String accessToken,
            String companyId,
            String properties
    ) {

        String url =
                "https://api.hubapi.com/crm/v3/objects/companies/"
                        + companyId
                        + "?properties="
                        + properties
                        + "&associations=deals"
                        + "&archived=false";

        try {

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setBearerAuth(
                    accessToken
            );

            headers.setAccept(
                    MediaType.parseMediaTypes(
                            MediaType.APPLICATION_JSON_VALUE
                    )
            );

            HttpEntity<String> request =
                    new HttpEntity<>(
                            headers
                    );

            ResponseEntity<HubspotCompanyRawResponseDto> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            request,
                            HubspotCompanyRawResponseDto.class
                    );

            HubspotCompanyRawResponseDto body =
                    response.getBody();

            if (body == null
                    || body.getProperties() == null) {

                log.error(
                        "Company data not found for companyId: {}",
                        companyId
                );

                throw new SharkdomException(
                        ErrorMessages.SH91
                );
            }

            return mapToCompanyResponse(
                    body.getProperties(), body.getUrl()
            );

        } catch (Exception e) {

            log.error(
                    "Error while fetching company by id: {}",
                    companyId,
                    e
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );
        }
    }

    private HubspotCompanyResponseDto mapToCompanyResponse(
            Map<String, Object> properties, String url
    ) {

        HubspotCompanyResponseDto dto =
                new HubspotCompanyResponseDto();

        if (properties == null) {
            return dto;
        }

        dto.setName(
                (String) properties.get("name")
        );
        dto.setWebsite(
                url
        );
        dto.setIndustry(
                (String) properties.get("industry")
        );
        dto.setNumberOfEmployees(
                (String) properties.get("numberofemployees")
        );
        dto.setCountry(
                (String) properties.get("country")
        );
        dto.setLinkedInUrl(
                (String) properties.get("linkedin_company_page")
        );
        dto.setAnnualRevenue(
                (String) properties.get("annualrevenue")
        );
        dto.setDescription(
                (String) properties.get("description")
        );
        dto.setPhone(
                (String) properties.get("phone")
        );
        dto.setCity(
                (String) properties.get("city")
        );

        return dto;
    }

    private HubspotContactResponseDto mapToContactResponse(
            Map<String, Object> properties
    ) {

        HubspotContactResponseDto dto =
                new HubspotContactResponseDto();

        if (properties == null) {
            return dto;
        }

        dto.setEmail(
                (String) properties.get("email")
        );
        dto.setFullName(
                (String) properties.get("firstname")
        );
        dto.setJobTitle(
                (String) properties.get("jobtitle")
        );
        dto.setLinkedInUrl(
                (String) properties.get("hs_linkedin_url")
        );
        dto.setLeadStatus(
                (String) properties.get("hs_lead_status")
        );
        dto.setPhone(
                (String) properties.get("phone")
        );
        dto.setLastActivityDate(
                (String) properties.get("hs_notes_last_activity")
        );
        dto.setContactOwner(
                (String) properties.get("ownername")
        );

        return dto;
    }

    @Transactional
    public String getCompanyWebsiteByDealId(
            Long organizationId,
            String dealId
    ) {

        log.info(
                "Fetching company website for dealId: {} organizationId: {}",
                dealId,
                organizationId
        );

        DealCompanyAssociationResponse associationResponse =
                getDealCompanyAssociation(
                        organizationId,
                        dealId
                );

        if (associationResponse == null
                || associationResponse.getCompanyId() == null) {

            log.error(
                    "Company association not found for dealId: {}",
                    dealId
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );
        }

        HubspotCompanyResponseDto companyResponse =
                getCompanyById(
                        organizationId,
                        String.valueOf(
                                associationResponse.getCompanyId()
                        ),
                        "website"
                );

        if (companyResponse == null
                || companyResponse.getWebsite() == null) {

            log.error(
                    "Website not found for companyId: {}",
                    associationResponse.getCompanyId()
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );
        }

        log.info(
                "Successfully fetched website for companyId: {}",
                associationResponse.getCompanyId()
        );

        return companyResponse.getWebsite();
    }

    @Transactional
    public String getCompanyNumberOfEmployeesByDealId(
            Long organizationId,
            String dealId
    ) {

        log.info(
                "Fetching company number of employees for dealId: {} organizationId: {}",
                dealId,
                organizationId
        );

        DealCompanyAssociationResponse associationResponse =
                getDealCompanyAssociation(
                        organizationId,
                        dealId
                );

        if (associationResponse == null
                || associationResponse.getCompanyId() == null) {

            log.error(
                    "Company association not found for dealId: {}",
                    dealId
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );
        }

        HubspotCompanyResponseDto companyResponse =
                getCompanyById(
                        organizationId,
                        String.valueOf(
                                associationResponse.getCompanyId()
                        ),
                        "numberofemployees"
                );

        if (companyResponse == null) {

            log.error(
                    "Company details not found for companyId: {}",
                    associationResponse.getCompanyId()
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );
        }

        if (companyResponse.getNumberOfEmployees() == null) {

            log.warn(
                    "Number of employees not found for companyId: {}",
                    associationResponse.getCompanyId()
            );

            return null;
        }

        log.info(
                "Successfully fetched number of employees for companyId: {}",
                associationResponse.getCompanyId()
        );

        return companyResponse.getNumberOfEmployees();
    }

    @Transactional
    public String getCompanyIndustryByDealId(
            Long organizationId,
            String dealId
    ) {

        log.info(
                "Fetching company industry for dealId: {} organizationId: {}",
                dealId,
                organizationId
        );

        DealCompanyAssociationResponse associationResponse =
                getDealCompanyAssociation(
                        organizationId,
                        dealId
                );

        if (associationResponse == null
                || associationResponse.getCompanyId() == null) {

            log.error(
                    "Company association not found for dealId: {}",
                    dealId
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );
        }

        HubspotCompanyResponseDto companyResponse =
                getCompanyById(
                        organizationId,
                        String.valueOf(
                                associationResponse.getCompanyId()
                        ),
                        "industry"
                );

        if (companyResponse == null) {

            log.error(
                    "Company details not found for companyId: {}",
                    associationResponse.getCompanyId()
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );
        }

        if (companyResponse.getIndustry() == null) {

            log.warn(
                    "Industry not found for companyId: {}",
                    associationResponse.getCompanyId()
            );

            return null;
        }

        log.info(
                "Successfully fetched industry for companyId: {}",
                associationResponse.getCompanyId()
        );

        return companyResponse.getIndustry();
    }

    @Transactional
    public String getCompanyCountryByDealId(
            Long organizationId,
            String dealId
    ) {

        log.info(
                "Fetching company country for dealId: {} organizationId: {}",
                dealId,
                organizationId
        );

        DealCompanyAssociationResponse associationResponse =
                getDealCompanyAssociation(
                        organizationId,
                        dealId
                );

        if (associationResponse == null
                || associationResponse.getCompanyId() == null) {

            log.error(
                    "Company association not found for dealId: {}",
                    dealId
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );
        }

        HubspotCompanyResponseDto companyResponse =
                getCompanyById(
                        organizationId,
                        String.valueOf(
                                associationResponse.getCompanyId()
                        ),
                        "country"
                );

        if (companyResponse == null) {

            log.error(
                    "Company details not found for companyId: {}",
                    associationResponse.getCompanyId()
            );

            throw new SharkdomException(
                    ErrorMessages.SH91
            );
        }

        if (companyResponse.getCountry() == null) {

            log.warn(
                    "Country not found for companyId: {}",
                    associationResponse.getCompanyId()
            );

            return null;
        }

        log.info(
                "Successfully fetched country for companyId: {}",
                associationResponse.getCompanyId()
        );

        return companyResponse.getCountry();
    }
}
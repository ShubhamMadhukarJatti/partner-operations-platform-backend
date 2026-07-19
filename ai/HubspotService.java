package com.sharkdom.service.ai;


import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.model.ai.HubspotTokenResponse;
import com.sharkdom.model.ai.PersonaMode;
import com.sharkdom.model.ai.RecordType;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.salesforce.service.SalesforceAuthService;
import com.sharkdom.salesforce.service.SalesforceService;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class HubspotService {
    private final RestTemplate restTemplate;
    @Value("${hubspot.id}")
    private String hubspotId;
    @Value("${hubspot.secret}")
    private String hubspotSecret;
    @Value("${env}")
    private String env;

    private final IntegrationRepository integrationRepository;
    private final SalesforceService salesforceService;
    private final SalesforceAuthService salesforceAuthService;

    public HubspotService(IntegrationRepository integrationRepository, SalesforceService salesforceService, SalesforceAuthService salesforceAuthService) {
        this.integrationRepository = integrationRepository;
        this.salesforceService = salesforceService;
        this.salesforceAuthService = salesforceAuthService;
        restTemplate = new RestTemplate();
    }

    @Transactional
    public Map<Object, Object> getDetails(Long organizationId, String fields) {
        var details = integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.HUBSPOT);
        var refreshToken = details.getRefreshToken();
        var accessToken = generateAccessToken(refreshToken);
        return getData(accessToken, fields);
    }

    @Transactional
    public Map<Object, Object> getDetailsWithLimitAndAfter(Long organizationId, String fields, String after, Long limit) {
        var details = integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.HUBSPOT);
        var refreshToken = details.getRefreshToken();
        var accessToken = generateAccessToken(refreshToken);
        return getDataWithLimitAndAfter(accessToken, fields,after,limit);
    }

    private Map<Object, Object> getDataWithLimitAndAfter(
            String accessToken,
            String fields,
            String after,
            Long limit) {

        String url = "https://api.hubapi.com/crm/v3/objects/contacts?properties=" + fields;

        if (limit != null) {
            url += "&limit=" + limit;
        }

        if (after != null && !after.isEmpty()) {
            url += "&after=" + after;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ParameterizedTypeReference<Map<Object, Object>> responseType =
                new ParameterizedTypeReference<>() {};

        ResponseEntity<Map<Object, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                responseType
        );

        return response.getBody();
    }
    @Transactional
    public List<String> getContacts() {
        Long organizationId = Util.getOrgIdFromToken();
        var details = integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.HUBSPOT);
        var refreshToken = details.getRefreshToken();
        var accessToken = generateAccessToken(refreshToken);
        return getContacts(accessToken);
    }

    private Map<Object, Object> getData(String accessToken, String fields) {
        System.out.println("accessToken: " + accessToken);

        String url = "https://api.hubapi.com/crm/v3/objects/contacts?properties="
                + fields + "&limit=100";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ParameterizedTypeReference<Map<Object, Object>> responseType =
                new ParameterizedTypeReference<>() {};

        ResponseEntity<Map<Object, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                responseType
        );

        return response.getBody();
    }

    private List<String> getContacts(String accessToken) {
        String url = "https://api.hubapi.com/crm/v3/properties/contacts";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ParameterizedTypeReference<Map<Object, Object>> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<Map<Object, Object>> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType);
        var responseBody = response.getBody();
        List<LinkedHashMap<String, Object>> entries = (List<LinkedHashMap<String, Object>>) responseBody.get("results");
        List<String> names = entries.stream()
                .map(entry -> (String) entry.get("name"))
                .collect(Collectors.toList());
        return names;
    }

    private String generateAccessToken(String refreshToken) {
        String url = "https://api.hubapi.com/oauth/v1/token";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", hubspotId);
        body.add("client_secret", hubspotSecret);
        body.add("refresh_token", refreshToken);

        // Combine headers and body into an HttpEntity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // Make the POST request
        HubspotTokenResponse response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, HubspotTokenResponse.class).getBody();
        if (response != null) {
            return response.getAccess_token();
        } else {
            throw new SharkdomException(ErrorMessages.SH91);
        }
    }

    @Transactional
    public Map<Object, Object> getDetailsByUserId(String userId, String fields) {
        var details = integrationRepository.findByUserIdAndIntegrationType(userId, IntegrationType.HUBSPOT);
        var refreshToken = details.getRefreshToken();
        var accessToken = generateAccessToken(refreshToken);
        return getData(accessToken, fields);
    }

    @Transactional
    public List<String> getContactsByUserId(String userId) {
        var details = integrationRepository.findByUserIdAndIntegrationType(userId, IntegrationType.HUBSPOT);
        var refreshToken = details.getRefreshToken();
        var accessToken = generateAccessToken(refreshToken);
        return getContacts(accessToken);
    }

    @Transactional
    public Map<Object, Object> getDetailsForVersioning(Long organizationId, List<String> fields) {
        var details = integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.HUBSPOT);
        var refreshToken = details.getRefreshToken();
        var accessToken = generateAccessToken(refreshToken);
        return getDataForVersioning(accessToken, fields);
    }

//    @Transactional
//    public Map<Object, Object> getDetailsForVersioningSalesForce(Long organizationId, List<String> fields) {
//        var details = integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.SALESFORCE);
//        var refreshToken = details.getRefreshToken();
//        var accessToken = salesforceAuthService.refreshAccessToken(refreshToken);
////        return getDataForVersioning(accessToken, fields);
//    }

    private Map<Object, Object> getDataForVersioning(String accessToken, List<String> fields) {

        String properties = String.join(",", fields);

        String url =
                "https://api.hubapi.com/crm/v3/objects/contacts?properties="
                        + properties;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> requestEntity =
                new HttpEntity<>(headers);

        ResponseEntity<Map<Object, Object>> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<>() {}
                );

        return response.getBody();
    }

    @Transactional
    public Map<Object, Object> getDealProperties(Long organizationId) {

        log.info("Fetching HubSpot Deal Properties for organizationId: {}", organizationId);

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.HUBSPOT);

        if (details == null || details.getRefreshToken() == null) {
            log.error("HubSpot integration not found for organizationId: {}", organizationId);
            throw new SharkdomException(ErrorMessages.SH91);
        }

        String refreshToken = details.getRefreshToken();
        String accessToken = generateAccessToken(refreshToken);

        return getDealPropertiesData(accessToken);
    }

    private Map<Object, Object> getDealPropertiesData(String accessToken) {

        String url = "https://api.hubapi.com/crm/v3/properties/deals";

        log.info("Calling HubSpot API: {}", url);

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ParameterizedTypeReference<Map<Object, Object>> responseType =
                    new ParameterizedTypeReference<>() {};

            ResponseEntity<Map<Object, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    responseType
            );

            log.info("HubSpot API Response Status: {}", response.getStatusCode());

            return response.getBody();

        }
        catch (HttpClientErrorException e) {

            log.error("HubSpot Client Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());

            throw new SharkdomException(ErrorMessages.valueOf("HubSpot Client Error : " + e.getResponseBodyAsString()));

        }
        catch (HttpServerErrorException e) {

            log.error("HubSpot Server Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());

            throw new SharkdomException(ErrorMessages.valueOf("HubSpot Server Error : " + e.getResponseBodyAsString()));

        }
        catch (Exception e) {

            log.error("Unexpected error while calling HubSpot Deals Properties API", e);

            throw new SharkdomException(ErrorMessages.valueOf("Unable to fetch HubSpot deal properties"));
        }
    }

    @Transactional
    public Map<Object, Object> getDeals(Long organizationId, String fields) {

        log.info("Fetching HubSpot deals for organizationId: {}", organizationId);

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.HUBSPOT);

        if (details == null || details.getRefreshToken() == null) {
            log.error("HubSpot integration not found for organizationId: {}", organizationId);
            throw new SharkdomException(ErrorMessages.SH91);
        }

        String refreshToken = details.getRefreshToken();
        String accessToken = generateAccessToken(refreshToken);

        return getDealsData(accessToken, fields);
    }

    private Map<Object, Object> getDealsData(String accessToken, String fields) {

        String url = "https://api.hubapi.com/crm/v3/objects/deals?properties="
                + fields + "&limit=100";

        log.info("Calling HubSpot Deals API: {}", url);

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ParameterizedTypeReference<Map<Object, Object>> responseType =
                    new ParameterizedTypeReference<>() {};

            ResponseEntity<Map<Object, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    responseType
            );

            log.info("HubSpot Deals API Response Status: {}", response.getStatusCode());

            return response.getBody();

        } catch (HttpClientErrorException e) {

            log.error("HubSpot Client Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SharkdomException(ErrorMessages.valueOf("HubSpot Client Error: " + e.getResponseBodyAsString()));

        } catch (HttpServerErrorException e) {

            log.error("HubSpot Server Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SharkdomException(ErrorMessages.valueOf("HubSpot Server Error: " + e.getResponseBodyAsString()));

        } catch (Exception e) {

            log.error("Unexpected error while fetching HubSpot deals", e);
            throw new SharkdomException(ErrorMessages.valueOf("Unable to fetch HubSpot deals"));
        }
    }


    @Transactional
    public Map<Object, Object> getDealsAfterAndLimit(Long organizationId, String fields,String after,Long limit) {

        log.info("Fetching HubSpot deals for organizationId: {}", organizationId);

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.HUBSPOT);

        if (details == null || details.getRefreshToken() == null) {
            log.error("HubSpot integration not found for organizationId: {}", organizationId);
            throw new SharkdomException(ErrorMessages.SH91);
        }

        String refreshToken = details.getRefreshToken();
        String accessToken = generateAccessToken(refreshToken);

        return getDealsDataAfterAndLimit(accessToken, fields,after,limit);
    }

    private Map<Object, Object> getDealsDataAfterAndLimit(
            String accessToken,
            String fields,
            String after,
            Long limit) {

        StringBuilder urlBuilder = new StringBuilder(
                "https://api.hubapi.com/crm/v3/objects/deals?properties=" + fields
        );

        // add limit
        if (limit != null) {
            urlBuilder.append("&limit=").append(limit);
        }

        // add after (pagination cursor)
        if (after != null && !after.isEmpty()) {
            urlBuilder.append("&after=").append(after);
        }

        String url = urlBuilder.toString();

        log.info("Calling HubSpot Deals API: {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ParameterizedTypeReference<Map<Object, Object>> responseType =
                    new ParameterizedTypeReference<>() {};

            ResponseEntity<Map<Object, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    responseType
            );

            log.info("HubSpot Deals API Response Status: {}", response.getStatusCode());

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("HubSpot Client Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SharkdomException(ErrorMessages.valueOf(e.getResponseBodyAsString()));

        } catch (HttpServerErrorException e) {
            log.error("HubSpot Server Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SharkdomException(ErrorMessages.valueOf(e.getResponseBodyAsString()));

        } catch (Exception e) {
            log.error("Unexpected error while fetching HubSpot deals", e);
            throw new SharkdomException(ErrorMessages.valueOf("Unable to fetch HubSpot deals"));
        }
    }

    @Transactional
    public Map<Object, Object> getCompanyProperties(Long organizationId) {

        log.info("Fetching HubSpot company properties for organizationId: {}", organizationId);

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.HUBSPOT);

        if (details == null || details.getRefreshToken() == null) {
            log.error("HubSpot integration not found for organizationId: {}", organizationId);
            throw new SharkdomException(ErrorMessages.SH91);
        }

        String refreshToken = details.getRefreshToken();
        String accessToken = generateAccessToken(refreshToken);

        return getCompanyPropertiesData(accessToken);
    }

    private Map<Object, Object> getCompanyPropertiesData(String accessToken) {

        String url = "https://api.hubapi.com/crm/v3/properties/companies";

        log.info("Calling HubSpot API: {}", url);

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ParameterizedTypeReference<Map<Object, Object>> responseType =
                    new ParameterizedTypeReference<>() {};

            ResponseEntity<Map<Object, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    responseType
            );

            log.info("HubSpot Company Properties Response Status: {}", response.getStatusCode());

            return response.getBody();

        } catch (HttpClientErrorException e) {

            log.error("HubSpot Client Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SharkdomException(ErrorMessages.valueOf("HubSpot Client Error: " + e.getResponseBodyAsString()));

        } catch (HttpServerErrorException e) {

            log.error("HubSpot Server Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SharkdomException(ErrorMessages.valueOf("HubSpot Server Error: " + e.getResponseBodyAsString()));

        } catch (Exception e) {

            log.error("Unexpected error while calling HubSpot Companies Properties API", e);
            throw new SharkdomException(ErrorMessages.valueOf("Unable to fetch HubSpot company properties"));
        }
    }

    @Transactional
    public Map<Object, Object> getCompanies(Long organizationId, String fields) {

        log.info("Fetching HubSpot companies for organizationId: {}", organizationId);

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.HUBSPOT);

        if (details == null || details.getRefreshToken() == null) {
            log.error("HubSpot integration not found for organizationId: {}", organizationId);
            throw new SharkdomException(ErrorMessages.SH91);
        }

        String refreshToken = details.getRefreshToken();
        String accessToken = generateAccessToken(refreshToken);

        return getCompaniesData(accessToken, fields);
    }

    @Transactional
    public Map<Object, Object> getCompaniesAfterAndLimit(Long organizationId, String fields,String after,Long limit) {

        log.info("Fetching HubSpot companies for organizationId: {}", organizationId);

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.HUBSPOT);

        if (details == null || details.getRefreshToken() == null) {
            log.error("HubSpot integration not found for organizationId: {}", organizationId);
            throw new SharkdomException(ErrorMessages.SH91);
        }

        String refreshToken = details.getRefreshToken();
        String accessToken = generateAccessToken(refreshToken);

        return getCompaniesDataAfterAndLimit(accessToken, fields,after,limit);
    }

    private Map<Object, Object> getCompaniesData(String accessToken, String fields) {

        String url = "https://api.hubapi.com/crm/v3/objects/companies?properties=" + fields;

        log.info("Calling HubSpot Companies API: {}", url);

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ParameterizedTypeReference<Map<Object, Object>> responseType =
                    new ParameterizedTypeReference<>() {};

            ResponseEntity<Map<Object, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    responseType
            );

            log.info("HubSpot Companies API Response Status: {}", response.getStatusCode());

            return response.getBody();

        }
        catch (HttpClientErrorException e) {

            log.error("HubSpot Client Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SharkdomException(ErrorMessages.valueOf("HubSpot Client Error: " + e.getResponseBodyAsString()));

        }
        catch (HttpServerErrorException e) {

            log.error("HubSpot Server Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SharkdomException(ErrorMessages.valueOf("HubSpot Server Error: " + e.getResponseBodyAsString()));

        }
        catch (Exception e) {

            log.error("Unexpected error while fetching HubSpot companies", e);
            throw new SharkdomException(ErrorMessages.valueOf("Unable to fetch HubSpot companies"));
        }
    }


    private Map<Object, Object> getCompaniesDataAfterAndLimit(
            String accessToken,
            String fields,
            String after,
            Long limit) {

        StringBuilder urlBuilder = new StringBuilder(
                "https://api.hubapi.com/crm/v3/objects/companies?properties=" + fields
        );

        // add limit
        if (limit != null) {
            urlBuilder.append("&limit=").append(limit);
        }

        // add after (pagination cursor)
        if (after != null && !after.isEmpty()) {
            urlBuilder.append("&after=").append(after);
        }

        String url = urlBuilder.toString();

        log.info("Calling HubSpot Companies API: {}", url);

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ParameterizedTypeReference<Map<Object, Object>> responseType =
                    new ParameterizedTypeReference<>() {};

            ResponseEntity<Map<Object, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    responseType
            );

            log.info("HubSpot Companies API Response Status: {}", response.getStatusCode());

            return response.getBody();

        } catch (HttpClientErrorException e) {

            log.error("HubSpot Client Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SharkdomException(ErrorMessages.valueOf(e.getResponseBodyAsString()));

        } catch (HttpServerErrorException e) {

            log.error("HubSpot Server Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SharkdomException(ErrorMessages.valueOf(e.getResponseBodyAsString()));

        } catch (Exception e) {

            log.error("Unexpected error while fetching HubSpot companies", e);
            throw new SharkdomException(ErrorMessages.valueOf("Unable to fetch HubSpot companies"));
        }
    }

    @Transactional
    public Map<Object, Object> getDealCompanyAssociations(Long organizationId, String dealId) {

        log.info("Fetching companies associated with dealId: {} for organizationId: {}", dealId, organizationId);

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.HUBSPOT);

        if (details == null || details.getRefreshToken() == null) {
            log.error("HubSpot integration not found for organizationId: {}", organizationId);
            throw new SharkdomException(ErrorMessages.SH91);
        }

        String refreshToken = details.getRefreshToken();
        String accessToken = generateAccessToken(refreshToken);

        return getDealCompanyAssociationsData(accessToken, dealId);
    }

    private Map<Object, Object> getDealCompanyAssociationsData(String accessToken, String dealId) {

        String url = "https://api.hubapi.com/crm/v4/objects/deals/" + dealId + "/associations/companies";

        log.info("Calling HubSpot Deal → Company Associations API: {}", url);

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ParameterizedTypeReference<Map<Object, Object>> responseType =
                    new ParameterizedTypeReference<>() {};

            ResponseEntity<Map<Object, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    responseType
            );

            log.info("HubSpot Associations Response Status: {}", response.getStatusCode());

            return response.getBody();

        } catch (HttpClientErrorException e) {

            log.error("HubSpot Client Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SharkdomException(ErrorMessages.valueOf("HubSpot Client Error: " + e.getResponseBodyAsString()));

        } catch (HttpServerErrorException e) {

            log.error("HubSpot Server Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SharkdomException(ErrorMessages.valueOf("HubSpot Server Error: " + e.getResponseBodyAsString()));

        } catch (Exception e) {

            log.error("Unexpected error while fetching deal-company associations", e);
            throw new SharkdomException(ErrorMessages.valueOf("Unable to fetch deal-company associations"));
        }
    }

    @Transactional
    public Map<Object, Object> getContactCompanyAssociations(Long organizationId, String contactId) {

        log.info("Fetching companies associated with contactId: {} for organizationId: {}", contactId, organizationId);

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.HUBSPOT);

        if (details == null || details.getRefreshToken() == null) {
            log.error("HubSpot integration not found for organizationId: {}", organizationId);
            throw new SharkdomException(ErrorMessages.SH91);
        }

        String refreshToken = details.getRefreshToken();
        String accessToken = generateAccessToken(refreshToken);

        return getContactCompanyAssociationsData(accessToken, contactId);
    }

    private Map<Object, Object> getContactCompanyAssociationsData(String accessToken, String contactId) {

        String url = "https://api.hubapi.com/crm/v4/objects/contacts/" + contactId + "/associations/companies";

        log.info("Calling HubSpot Contact → Company Associations API: {}", url);

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ParameterizedTypeReference<Map<Object, Object>> responseType =
                    new ParameterizedTypeReference<>() {};

            ResponseEntity<Map<Object, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    responseType
            );

            log.info("HubSpot Associations Response Status: {}", response.getStatusCode());

            return response.getBody();

        } catch (HttpClientErrorException e) {

            log.error("HubSpot Client Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SharkdomException(ErrorMessages.valueOf("HubSpot Client Error: " + e.getResponseBodyAsString()));

        } catch (HttpServerErrorException e) {

            log.error("HubSpot Server Error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SharkdomException(ErrorMessages.valueOf("HubSpot Server Error: " + e.getResponseBodyAsString()));

        } catch (Exception e) {

            log.error("Unexpected error while fetching contact-company associations", e);
            throw new SharkdomException(ErrorMessages.valueOf("Unable to fetch contact-company associations"));
        }
    }

    @Transactional
    public Map<String, Object> getHubspotMetadata() {

        Long organizationId = Util.getOrgIdFromToken();

        log.info("Fetching HubSpot metadata for organizationId: {}", organizationId);

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.HUBSPOT);

        if (details == null || details.getRefreshToken() == null) {
            log.error("HubSpot integration not found for organizationId: {}", organizationId);
            throw new SharkdomException(ErrorMessages.SH91);
        }

        String refreshToken = details.getRefreshToken();
        String accessToken = generateAccessToken(refreshToken);

        Map<String, Object> response = new LinkedHashMap<>();

        try {

            Map<Object, Object> deals = getDealPropertiesData(accessToken);

            List<String> contacts = getContacts(accessToken);

            Map<Object, Object> companies = getCompanyPropertiesData(accessToken);

            response.put("deals", deals);
            response.put("contacts", contacts);
            response.put("companies", companies);

            return response;

        } catch (Exception e) {

            log.error("Error fetching HubSpot metadata", e);
            throw new SharkdomException(ErrorMessages.valueOf("Unable to fetch HubSpot metadata"));
        }
    }

    public Map<Object, Object> getObjectData(
            Long orgId,
            String objectType,
            String properties) {

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(
                        orgId,
                        IntegrationType.HUBSPOT
                );

        String accessToken = generateAccessToken(details.getRefreshToken());

        String url = switch (objectType) {
            case "CONTACTS" ->
                    "https://api.hubapi.com/crm/v3/objects/contacts?properties=" + properties;

            case "COMPANIES" ->
                    "https://api.hubapi.com/crm/v3/objects/companies?properties=" + properties;

            case "DEALS" ->
                    "https://api.hubapi.com/crm/v3/objects/deals?properties=" + properties;

            default -> throw new RuntimeException("Invalid object type");
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<Map<Object, Object>>() {}
        ).getBody();
    }


    @Transactional
    public Map<Object, Object> fetchHubspotPersonaData(
            RecordType recordType,
            String properties
    ) {

        Long organizationId=Util.getOrgIdFromToken();

        log.info("Persona Data Fetch Started | orgId={} | recordType={} ",
                organizationId, recordType);

        // Fetch integration details
        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.HUBSPOT
                );

        if (details == null || details.getRefreshToken() == null) {
            log.error("HubSpot integration not found | orgId={}", organizationId);
            throw new SharkdomException(ErrorMessages.SH91);
        }

        String refreshToken = details.getRefreshToken();
        String accessToken;

        try {
            accessToken = generateAccessToken(refreshToken);
            log.info("Access token generated successfully for orgId={}", organizationId);
        } catch (Exception e) {
            log.error("Error generating access token | orgId={}", organizationId, e);
            throw new SharkdomException(ErrorMessages.SH91);
        }

        try {

            Map<Object, Object> response;

            switch (recordType) {

                case CUSTOMER -> {
                    log.info("Fetching HubSpot Companies | orgId={}", organizationId);
                    response = getCompaniesData(accessToken, properties);
                }

                case PROSPECT -> {
                    log.info("Fetching HubSpot Contacts | orgId={}", organizationId);
                    response = getData(accessToken, properties);
                }

                case OPPORTUNITY -> {
                    log.info("Fetching HubSpot Deals | orgId={}", organizationId);
                    response = getDealsData(accessToken, properties);
                }

                default -> {
                    log.error("Invalid RecordType received: {}", recordType);
                    throw new SharkdomException(ErrorMessages.SH91);
                }
            }

            log.info("Persona Data Fetch Completed Successfully | orgId={} | recordType={}",
                    organizationId, recordType);

            return response;

        } catch (HttpClientErrorException e) {

            log.error("HubSpot Client Error | status={} | body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            throw new SharkdomException(
                    ErrorMessages.valueOf("HubSpot Client Error: " + e.getResponseBodyAsString())
            );

        } catch (HttpServerErrorException e) {

            log.error("HubSpot Server Error | status={} | body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            throw new SharkdomException(
                    ErrorMessages.valueOf("HubSpot Server Error: " + e.getResponseBodyAsString())
            );

        } catch (Exception e) {

            log.error("Unexpected error while fetching persona data | orgId={} | recordType={}",
                    organizationId, recordType, e);

            throw new SharkdomException(
                    ErrorMessages.valueOf("Unable to fetch persona data from HubSpot")
            );
        }
    }

    /**
     * Fetches HubSpot metadata dynamically based on requested record types.
     *
     * <p>
     * Supports:
     * CUSTOMER -> Companies
     * PROSPECT -> Contacts
     * OPPORTUNITY -> Deals
     * </p>
     *
     * @param recordTypes list of record types
     * @return aggregated response map
     */
    @Transactional
    public Map<String, Object> getHubspotDataByRecordTypes(List<RecordType> recordTypes) {

        Long orgId = Util.getOrgIdFromToken();

        log.info("Fetching HubSpot dynamic data | orgId={} | recordTypes={}", orgId, recordTypes);

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(orgId, IntegrationType.HUBSPOT);

        if (details == null || details.getRefreshToken() == null) {
            log.error("HubSpot integration not found | orgId={}", orgId);
            throw new SharkdomException(ErrorMessages.SH91);
        }

        String accessToken = generateAccessToken(details.getRefreshToken());

        Map<String, Object> response = new LinkedHashMap<>();

        try {

            for (RecordType type : recordTypes) {

                switch (type) {

                    case CUSTOMER -> {
                        log.info("Fetching Companies data | orgId={}", orgId);
                        response.put("companies", getCompanyPropertiesData(accessToken));
                    }

                    case PROSPECT -> {
                        log.info("Fetching Contacts data | orgId={}", orgId);
                        response.put("contacts", getContacts(accessToken));
                    }

                    case OPPORTUNITY -> {
                        log.info("Fetching Deals data | orgId={}", orgId);
                        response.put("deals", getDealPropertiesData(accessToken));
                    }

                    default -> log.warn("Unknown RecordType skipped: {}", type);
                }
            }

            log.info("HubSpot dynamic data fetch completed | orgId={}", orgId);

            return response;

        } catch (Exception e) {
            log.error("Error fetching HubSpot dynamic data | orgId={}", orgId, e);
            throw new SharkdomException(ErrorMessages.SH91);
        }
    }

    /**
     * Fetch HubSpot persona data based on single record type.
     *
     * <p>
     * CUSTOMER -> companies
     * PROSPECT -> contacts
     * OPPORTUNITY -> deals
     * </p>
     *
     * @param recordType type of record
     * @param properties comma separated properties
     * @return response map with appropriate key
     */
    @Transactional
    public Map<String, Object> fetchHubspotPersonaDataBulk(
            RecordType recordType,
            String properties
    ) {

        Long organizationId = Util.getOrgIdFromToken();

        log.info("Persona Fetch Started | orgId={} | recordType={} | properties={}",
                organizationId, recordType, properties);

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.HUBSPOT
                );

        if (details == null || details.getRefreshToken() == null) {
            log.error("HubSpot integration not found | orgId={}", organizationId);
            throw new SharkdomException(ErrorMessages.SH91);
        }

        String accessToken;
        try {
            accessToken = generateAccessToken(details.getRefreshToken());
            log.info("Access token generated successfully | orgId={}", organizationId);
        } catch (Exception e) {
            log.error("Error generating access token | orgId={}", organizationId, e);
            throw new SharkdomException(ErrorMessages.SH91);
        }

        Map<String, Object> response = new LinkedHashMap<>();

        try {

            switch (recordType) {

                case CUSTOMER -> {
                    log.info("Fetching HubSpot Companies | orgId={}", organizationId);
                    response.put("companies", getCompaniesData(accessToken, properties));
                }

                case PROSPECT -> {
                    log.info("Fetching HubSpot Contacts | orgId={}", organizationId);
                    response.put("contacts", getData(accessToken, properties));
                }

                case OPPORTUNITY -> {
                    log.info("Fetching HubSpot Deals | orgId={}", organizationId);
                    response.put("deals", getDealsData(accessToken, properties));
                }

                default -> {
                    log.error("Invalid RecordType received: {}", recordType);
                    throw new SharkdomException(ErrorMessages.SH91);
                }
            }

            log.info("Persona Fetch Completed | orgId={} | recordType={}",
                    organizationId, recordType);

            return response;

        } catch (HttpClientErrorException e) {

            log.error("HubSpot Client Error | status={} | body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            throw new SharkdomException(
                    ErrorMessages.valueOf("HubSpot Client Error: " + e.getResponseBodyAsString())
            );

        } catch (HttpServerErrorException e) {

            log.error("HubSpot Server Error | status={} | body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            throw new SharkdomException(
                    ErrorMessages.valueOf("HubSpot Server Error: " + e.getResponseBodyAsString())
            );

        } catch (Exception e) {

            log.error("Unexpected error | orgId={} | recordType={}",
                    organizationId, recordType, e);

            throw new SharkdomException(
                    ErrorMessages.valueOf("Unable to fetch persona data from HubSpot")
            );
        }
    }

    @Transactional
    public Map<Object, Object> createContact(Map<String, Object> payload) {

        Long orgId = Util.getOrgIdFromToken();

        log.info("Creating HubSpot Contact | orgId={}", orgId);

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(
                        orgId,
                        IntegrationType.HUBSPOT
                );

        if (details == null || details.getRefreshToken() == null) {
            log.error("HubSpot integration not found | orgId={}", orgId);
            throw new SharkdomException(ErrorMessages.SH91);
        }

        String accessToken = generateAccessToken(details.getRefreshToken());

        return createContactData(accessToken, payload);
    }

    private Map<Object, Object> createContactData(
            String accessToken,
            Map<String, Object> payload
    ) {

        String url = "https://api.hubapi.com/crm/v3/objects/contacts";

        log.info("Calling HubSpot Create Contact API");

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<Map<String, Object>> requestEntity =
                    new HttpEntity<>(payload, headers);

            ResponseEntity<Map<Object, Object>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            requestEntity,
                            new ParameterizedTypeReference<>() {}
                    );

            log.info("Contact created successfully | status={}", response.getStatusCode());

            return response.getBody();

        } catch (HttpClientErrorException e) {

            log.error("HubSpot Client Error: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            throw new SharkdomException(
                    ErrorMessages.valueOf("HubSpot Client Error: " + e.getResponseBodyAsString())
            );

        } catch (HttpServerErrorException e) {

            log.error("HubSpot Server Error: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            throw new SharkdomException(
                    ErrorMessages.valueOf("HubSpot Server Error: " + e.getResponseBodyAsString())
            );

        } catch (Exception e) {

            log.error("Unexpected error while creating contact", e);

            throw new SharkdomException(
                    ErrorMessages.valueOf("Unable to create HubSpot contact")
            );
        }
    }

    @Transactional
    public Map<Object, Object> getCompanyById(
            Long organizationId,
            String companyId,
            String properties
    ) {

        log.info("Fetching HubSpot company by id: {} for organizationId: {}",
                companyId,
                organizationId
        );

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.HUBSPOT
                );

        if (details == null || details.getRefreshToken() == null) {
            log.error("HubSpot integration not found for organizationId: {}",
                    organizationId);
            throw new SharkdomException(ErrorMessages.SH91);
        }

        String refreshToken = details.getRefreshToken();
        String accessToken = generateAccessToken(refreshToken);

        return getCompanyByIdData(
                accessToken,
                companyId,
                properties
        );
    }

    private Map<Object, Object> getCompanyByIdData(
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

        log.info("Calling HubSpot Company By Id API: {}", url);

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/json");

            HttpEntity<String> requestEntity =
                    new HttpEntity<>(headers);

            ParameterizedTypeReference<Map<Object, Object>> responseType =
                    new ParameterizedTypeReference<>() {};

            ResponseEntity<Map<Object, Object>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            requestEntity,
                            responseType
                    );

            log.info("HubSpot Company By Id API Response Status: {}",
                    response.getStatusCode());

            return response.getBody();

        } catch (HttpClientErrorException e) {

            log.error("HubSpot Client Error: {} - {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());

            throw new SharkdomException(
                    ErrorMessages.valueOf(
                            "HubSpot Client Error: "
                                    + e.getResponseBodyAsString()
                    )
            );

        } catch (HttpServerErrorException e) {

            log.error("HubSpot Server Error: {} - {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());

            throw new SharkdomException(
                    ErrorMessages.valueOf(
                            "HubSpot Server Error: "
                                    + e.getResponseBodyAsString()
                    )
            );

        } catch (Exception e) {

            log.error("Unexpected error while fetching company by id", e);

            throw new SharkdomException(
                    ErrorMessages.valueOf(
                            "Unable to fetch HubSpot company by id"
                    )
            );
        }
    }

    @Transactional
    public Map<Object, Object> getContactById(
            Long organizationId,
            String contactId,
            String properties
    ) {

        log.info(
                "Fetching HubSpot contact by id: {} for organizationId: {}",
                contactId,
                organizationId
        );

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.HUBSPOT
                );

        if (details == null || details.getRefreshToken() == null) {
            log.error(
                    "HubSpot integration not found for organizationId: {}",
                    organizationId
            );
            throw new SharkdomException(ErrorMessages.SH91);
        }

        String refreshToken = details.getRefreshToken();
        String accessToken = generateAccessToken(refreshToken);

        return getContactByIdData(
                accessToken,
                contactId,
                properties
        );
    }

    private Map<Object, Object> getContactByIdData(
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

        log.info("Calling HubSpot Contact By Id API: {}", url);

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/json");

            HttpEntity<String> requestEntity =
                    new HttpEntity<>(headers);

            ParameterizedTypeReference<Map<Object, Object>> responseType =
                    new ParameterizedTypeReference<>() {};

            ResponseEntity<Map<Object, Object>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            requestEntity,
                            responseType
                    );

            log.info(
                    "HubSpot Contact By Id API Response Status: {}",
                    response.getStatusCode()
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {

            log.error(
                    "HubSpot Client Error: {} - {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );

            throw new SharkdomException(
                    ErrorMessages.valueOf(
                            "HubSpot Client Error: "
                                    + e.getResponseBodyAsString()
                    )
            );

        } catch (HttpServerErrorException e) {

            log.error(
                    "HubSpot Server Error: {} - {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );

            throw new SharkdomException(
                    ErrorMessages.valueOf(
                            "HubSpot Server Error: "
                                    + e.getResponseBodyAsString()
                    )
            );

        } catch (Exception e) {

            log.error(
                    "Unexpected error while fetching contact by id",
                    e
            );

            throw new SharkdomException(
                    ErrorMessages.valueOf(
                            "Unable to fetch HubSpot contact by id"
                    )
            );
        }
    }

    @Transactional
    public Map<Object, Object> getDealContactAssociations(
            Long organizationId,
            String dealId
    ) {

        log.info(
                "Fetching contacts associated with dealId: {} for organizationId: {}",
                dealId,
                organizationId
        );

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.HUBSPOT
                );

        if (details == null || details.getRefreshToken() == null) {
            log.error(
                    "HubSpot integration not found for organizationId: {}",
                    organizationId
            );
            throw new SharkdomException(ErrorMessages.SH91);
        }

        String refreshToken = details.getRefreshToken();
        String accessToken = generateAccessToken(refreshToken);

        return getDealContactAssociationsData(
                accessToken,
                dealId
        );
    }

    private Map<Object, Object> getDealContactAssociationsData(
            String accessToken,
            String dealId
    ) {

        String url =
                "https://api.hubapi.com/crm/v4/objects/deals/"
                        + dealId
                        + "/associations/contacts";

        log.info(
                "Calling HubSpot Deal → Contact Associations API: {}",
                url
        );

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> requestEntity =
                    new HttpEntity<>(headers);

            ParameterizedTypeReference<Map<Object, Object>> responseType =
                    new ParameterizedTypeReference<>() {};

            ResponseEntity<Map<Object, Object>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            requestEntity,
                            responseType
                    );

            log.info(
                    "HubSpot Associations Response Status: {}",
                    response.getStatusCode()
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {

            log.error(
                    "HubSpot Client Error: {} - {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );

            throw new SharkdomException(
                    ErrorMessages.valueOf(
                            "HubSpot Client Error: "
                                    + e.getResponseBodyAsString()
                    )
            );

        } catch (HttpServerErrorException e) {

            log.error(
                    "HubSpot Server Error: {} - {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );

            throw new SharkdomException(
                    ErrorMessages.valueOf(
                            "HubSpot Server Error: "
                                    + e.getResponseBodyAsString()
                    )
            );

        } catch (Exception e) {

            log.error(
                    "Unexpected error while fetching deal-contact associations",
                    e
            );

            throw new SharkdomException(
                    ErrorMessages.valueOf(
                            "Unable to fetch deal-contact associations"
                    )
            );
        }
    }

    @Transactional
    public Map<Object, Object> getDealCompanyAssociationsV1(
            Long organizationId,
            String dealId
    ) {

        log.info(
                "Fetching companies associated with dealId: {} for organizationId: {}",
                dealId,
                organizationId
        );

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.HUBSPOT
                );

        if (details == null || details.getRefreshToken() == null) {
            log.error(
                    "HubSpot integration not found for organizationId: {}",
                    organizationId
            );
            throw new SharkdomException(ErrorMessages.SH91);
        }

        String refreshToken = details.getRefreshToken();
        String accessToken = generateAccessToken(refreshToken);

        return getDealCompanyAssociationsDataV1(
                accessToken,
                dealId
        );
    }

    private Map<Object, Object> getDealCompanyAssociationsDataV1(
            String accessToken,
            String dealId
    ) {

        String url =
                "https://api.hubapi.com/crm/v4/objects/deals/"
                        + dealId
                        + "/associations/companies";

        log.info(
                "Calling HubSpot Deal → Company Associations API: {}",
                url
        );

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> requestEntity =
                    new HttpEntity<>(headers);

            ParameterizedTypeReference<Map<Object, Object>> responseType =
                    new ParameterizedTypeReference<>() {};

            ResponseEntity<Map<Object, Object>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            requestEntity,
                            responseType
                    );

            log.info(
                    "HubSpot Associations Response Status: {}",
                    response.getStatusCode()
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {

            log.error(
                    "HubSpot Client Error: {} - {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );

            throw new SharkdomException(
                    ErrorMessages.valueOf(
                            "HubSpot Client Error: "
                                    + e.getResponseBodyAsString()
                    )
            );

        } catch (HttpServerErrorException e) {

            log.error(
                    "HubSpot Server Error: {} - {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );

            throw new SharkdomException(
                    ErrorMessages.valueOf(
                            "HubSpot Server Error: "
                                    + e.getResponseBodyAsString()
                    )
            );

        } catch (Exception e) {

            log.error(
                    "Unexpected error while fetching deal-company associations",
                    e
            );

            throw new SharkdomException(
                    ErrorMessages.valueOf(
                            "Unable to fetch deal-company associations"
                    )
            );
        }
    }

    @Transactional
    public Map<Object, Object> getOwnerById(
            Long organizationId,
            String ownerId
    ) {

        log.info(
                "Fetching HubSpot owner by id: {} for organizationId: {}",
                ownerId,
                organizationId
        );

        var details = integrationRepository
                .findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.HUBSPOT
                );

        if (details == null || details.getRefreshToken() == null) {

            log.error(
                    "HubSpot integration not found for organizationId: {}",
                    organizationId
            );

            throw new SharkdomException(ErrorMessages.SH91);
        }

        String refreshToken = details.getRefreshToken();
        String accessToken = generateAccessToken(refreshToken);

        return getOwnerByIdData(
                accessToken,
                ownerId
        );
    }

    private Map<Object, Object> getOwnerByIdData(
            String accessToken,
            String ownerId
    ) {

        String url =
                "https://api.hubapi.com/crm/v3/owners/" + ownerId;

        log.info("Calling HubSpot Owner API: {}", url);

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", "application/json");

            HttpEntity<String> requestEntity =
                    new HttpEntity<>(headers);

            ParameterizedTypeReference<Map<Object, Object>> responseType =
                    new ParameterizedTypeReference<>() {};

            ResponseEntity<Map<Object, Object>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            requestEntity,
                            responseType
                    );

            log.info(
                    "HubSpot Owner API Response Status: {}",
                    response.getStatusCode()
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {

            log.error(
                    "HubSpot Client Error: {} - {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );

            throw new SharkdomException(
                    ErrorMessages.valueOf(
                            "HubSpot Client Error: "
                                    + e.getResponseBodyAsString()
                    )
            );

        } catch (HttpServerErrorException e) {

            log.error(
                    "HubSpot Server Error: {} - {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );

            throw new SharkdomException(
                    ErrorMessages.valueOf(
                            "HubSpot Server Error: "
                                    + e.getResponseBodyAsString()
                    )
            );

        } catch (Exception e) {

            log.error(
                    "Unexpected error while fetching HubSpot owner by id",
                    e
            );

            throw new SharkdomException(
                    ErrorMessages.valueOf(
                            "Unable to fetch HubSpot owner by id"
                    )
            );
        }
    }

}

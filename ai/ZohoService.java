package com.sharkdom.service.ai;


import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.model.ai.ZohoToken;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class ZohoService {
    private final RestTemplate restTemplate;
    @Value("${hubspot.id}")
    private String hubspotId;
    @Value("${hubspot.secret}")
    private String hubspotSecret;
    @Value("${env}")
    private String env;
    private final IntegrationRepository integrationRepository;
    @Value("${zoho.client-id}")
    private String zohoId;
    @Value("${zoho.client-secret}")
    private String zohoSecret;



    public ZohoService(IntegrationRepository integrationRepository) {
        this.integrationRepository = integrationRepository;
        restTemplate = new RestTemplate();
    }

    @Transactional
    public ZohoToken generateToken(String authCode) throws URISyntaxException {

        return generateAccessToken(authCode);
    }

    private ZohoToken generateAccessToken(String authCode) throws URISyntaxException {
        var organizationId = Util.getOrgIdFromToken();
        var integrationDetails = integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.ZOHO);
        if (integrationDetails == null) {
            return new ZohoToken();
        }
        String url = integrationDetails.getPublishableKey() + "/oauth/v2/token";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", zohoId);
        body.add("client_secret", zohoSecret);
        body.add("code", authCode);
        body.add("redirect_uri", "https://dev.sharkdom.com");

        // Combine headers and body into an HttpEntity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // Make the POST request
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, ZohoToken.class).getBody();

    }

    public ZohoToken generateRefreshToken(String authCode, String publishableKey) {
        String url = publishableKey + "/oauth/v2/token";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", zohoId);
        body.add("client_secret", zohoSecret);
        body.add("refresh_token", authCode);

        // Combine headers and body into an HttpEntity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // Make the POST request
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, ZohoToken.class).getBody();

    }

    public Map generateDocusignToken(String authCode) {
        String url = "https://account-d.docusign.com/oauth/token?grant_type=authorization_code&code=" + authCode;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Authorization", "Basic YjJmNjFiNzUtNWM3Ny00OTU5LWEyN2UtZGRkMTZhMjVlOWY1OmIxMjhjYzVlLTI1MGYtNDIyNC04NDJkLTJlZWJlMDE1YzlmMQkgIA");

        // Combine headers and body into an HttpEntity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(headers);

        // Make the POST request
        return restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class).getBody();

    }

    public List<Map<String, Object>> getData() throws URISyntaxException {
        var organizationId = Util.getOrgIdFromToken();
        var integrationDetails = integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.ZOHO);
        if (integrationDetails == null) {
            return Collections.emptyList();
        }
        var accessToken = generateRefreshToken(integrationDetails.getRefreshToken(), integrationDetails.getPublishableKey());
        var domainExtension = getDomainExtension(integrationDetails.getPublishableKey());
        String url = "https://www.zohoapis." + domainExtension + "/crm/v2/Leads";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Zoho-oauthtoken " + accessToken.getAccess_token());
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(headers);
        Map<String, Object> body = restTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
        }).getBody();
        if (body == null || !body.containsKey("data")) {
            return List.of();
        }
        Object data = body.get("data");
        if (data instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(item -> (Map<String, Object>) item)
                    .toList();
        }
        return List.of();
    }

    private String getDomainExtension(String publishableKey) throws URISyntaxException {
        URI uri = new URI(publishableKey);
        String host = uri.getHost();
        if (host != null) {
            String[] parts = host.split("\\.");
            return parts[parts.length - 1];
        } else {
            throw new SharkdomException(ErrorMessages.SH116, "URL was incorrect, not able to convert into URI");
        }
    }

    public List<String> getFields() throws URISyntaxException {
        var records = getData();
        return records.isEmpty()
                ? List.of()
                : new ArrayList<>(records.get(0).keySet());
    }


    public List<Map<String, Object>> getDataByUserId(String userId) throws URISyntaxException {
        var integrationDetails = integrationRepository.findByUserIdAndIntegrationType(userId, IntegrationType.ZOHO);
        if (integrationDetails == null) {
            return Collections.emptyList();
        }
        var accessToken = generateRefreshToken(integrationDetails.getRefreshToken(), integrationDetails.getPublishableKey());
        var domainExtension = getDomainExtension(integrationDetails.getPublishableKey());
        String url = "https://www.zohoapis." + domainExtension + "/crm/v2/Leads";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Zoho-oauthtoken " + accessToken.getAccess_token());
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(headers);
        Map<String, Object> body = restTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
        }).getBody();
        if (body == null || !body.containsKey("data")) {
            return List.of();
        }
        Object data = body.get("data");
        if (data instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(item -> (Map<String, Object>) item)
                    .toList();
        }
        return List.of();
    }

    public List<String> getFieldsByUserId(String userId) throws URISyntaxException {
        var records = getDataByUserId(userId);
        return records.isEmpty()
                ? List.of()
                : new ArrayList<>(records.get(0).keySet());
    }

    public List<Map<String, Object>> getDealsData() throws URISyntaxException {

        var organizationId = Util.getOrgIdFromToken();

        var integrationDetails = integrationRepository
                .findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.ZOHO
                );

        if (integrationDetails == null) {
            return Collections.emptyList();
        }

        var accessToken = generateRefreshToken(
                integrationDetails.getRefreshToken(),
                integrationDetails.getPublishableKey()
        );

        var domainExtension =
                getDomainExtension(integrationDetails.getPublishableKey());

        String url =
                "https://www.zohoapis." + domainExtension + "/crm/v2/Deals";

        HttpHeaders headers = new HttpHeaders();

        headers.set(
                "Authorization",
                "Zoho-oauthtoken " + accessToken.getAccess_token()
        );

        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(headers);

        Map<String, Object> body = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
        ).getBody();

        if (body == null || !body.containsKey("data")) {
            return List.of();
        }

        Object data = body.get("data");

        if (data instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(item -> (Map<String, Object>) item)
                    .toList();
        }

        return List.of();
    }

    public List<String> getDealsFields() throws URISyntaxException {

        var records = getDealsData();

        return records.isEmpty()
                ? List.of()
                : new ArrayList<>(records.get(0).keySet());
    }


    public List<Map<String, Object>> getAccountsData() throws URISyntaxException {
        var organizationId = Util.getOrgIdFromToken();

        var integrationDetails = integrationRepository
                .findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.ZOHO
                );

        if (integrationDetails == null) {
            return Collections.emptyList();
        }

        var accessToken = generateRefreshToken(
                integrationDetails.getRefreshToken(),
                integrationDetails.getPublishableKey()
        );

        var domainExtension =
                getDomainExtension(integrationDetails.getPublishableKey());

        String url =
                "https://www.zohoapis." + domainExtension + "/crm/v2/Accounts";

        HttpHeaders headers = new HttpHeaders();
        headers.set(
                "Authorization",
                "Zoho-oauthtoken " + accessToken.getAccess_token()
        );

        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(headers);

        Map<String, Object> body = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
        ).getBody();

        if (body == null || !body.containsKey("data")) {
            return List.of();
        }

        Object data = body.get("data");

        if (data instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(item -> (Map<String, Object>) item)
                    .toList();
        }

        return List.of();
    }

    public List<String> getAccountsFields() throws URISyntaxException {
        var records = getAccountsData();

        return records.isEmpty()
                ? List.of()
                : new ArrayList<>(records.get(0).keySet());
    }


    public List<Map<String, Object>> getContactsData()
            throws URISyntaxException {

        var organizationId = Util.getOrgIdFromToken();

        var integrationDetails =
                integrationRepository.findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.ZOHO
                );

        if (integrationDetails == null) {
            return Collections.emptyList();
        }

        var accessToken = generateRefreshToken(
                integrationDetails.getRefreshToken(),
                integrationDetails.getPublishableKey()
        );

        var domainExtension =
                getDomainExtension(integrationDetails.getPublishableKey());

        String url =
                "https://www.zohoapis." +
                        domainExtension +
                        "/crm/v2/Contacts";

        HttpHeaders headers = new HttpHeaders();

        headers.set(
                "Authorization",
                "Zoho-oauthtoken " + accessToken.getAccess_token()
        );

        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(headers);

        Map<String, Object> body = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
        ).getBody();

        if (body == null || !body.containsKey("data")) {
            return List.of();
        }

        Object data = body.get("data");

        if (data instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(item -> (Map<String, Object>) item)
                    .toList();
        }

        return List.of();
    }

    public List<String> getContactsFields()
            throws URISyntaxException {

        var records = getContactsData();

        return records.isEmpty()
                ? List.of()
                : new ArrayList<>(records.get(0).keySet());
    }

    public String getAccountIdByAccountName(String accountName)
            throws URISyntaxException {

        var organizationId = Util.getOrgIdFromToken();

        var integrationDetails =
                integrationRepository.findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.ZOHO
                );

        if (integrationDetails == null) {
            return null;
        }

        var accessToken = generateRefreshToken(
                integrationDetails.getRefreshToken(),
                integrationDetails.getPublishableKey()
        );

        var domainExtension =
                getDomainExtension(integrationDetails.getPublishableKey());

        String url =
                "https://www.zohoapis." +
                        domainExtension +
                        "/crm/v2/Accounts/search?criteria=" +
                        "(Account_Name:equals:" + accountName + ")";

        HttpHeaders headers = new HttpHeaders();

        headers.set(
                "Authorization",
                "Zoho-oauthtoken " + accessToken.getAccess_token()
        );

        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(headers);

        Map<String, Object> body = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
        ).getBody();

        if (body == null || !body.containsKey("data")) {
            return null;
        }

        Object data = body.get("data");

        if (data instanceof List<?> list && !list.isEmpty()) {

            Object firstRecord = list.get(0);

            if (firstRecord instanceof Map<?, ?> map) {

                Object id = map.get("id");

                return id != null ? id.toString() : null;
            }
        }

        return null;
    }

    public Map<String, Object> getAccountByAccountId(String accountId)
            throws URISyntaxException {

        var organizationId = Util.getOrgIdFromToken();

        var integrationDetails =
                integrationRepository.findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.ZOHO
                );

        if (integrationDetails == null) {
            return Collections.emptyMap();
        }

        var accessToken = generateRefreshToken(
                integrationDetails.getRefreshToken(),
                integrationDetails.getPublishableKey()
        );

        var domainExtension =
                getDomainExtension(integrationDetails.getPublishableKey());

        String url =
                "https://www.zohoapis." +
                        domainExtension +
                        "/crm/v2/Accounts/" +
                        accountId;

        HttpHeaders headers = new HttpHeaders();

        headers.set(
                "Authorization",
                "Zoho-oauthtoken " + accessToken.getAccess_token()
        );

        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(headers);

        Map<String, Object> body = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
        ).getBody();

        if (body == null || !body.containsKey("data")) {
            return Collections.emptyMap();
        }

        Object data = body.get("data");

        if (data instanceof List<?> list && !list.isEmpty()) {

            Object firstRecord = list.get(0);

            if (firstRecord instanceof Map<?, ?> map) {

                return (Map<String, Object>) map;
            }
        }

        return Collections.emptyMap();
    }

    public String getDealIdByDealName(String dealName)
            throws URISyntaxException {

        var organizationId = Util.getOrgIdFromToken();

        var integrationDetails =
                integrationRepository.findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.ZOHO
                );

        if (integrationDetails == null) {
            return null;
        }

        var accessToken = generateRefreshToken(
                integrationDetails.getRefreshToken(),
                integrationDetails.getPublishableKey()
        );

        var domainExtension =
                getDomainExtension(integrationDetails.getPublishableKey());

        String url =
                "https://www.zohoapis." +
                        domainExtension +
                        "/crm/v2/Deals/search?criteria=" +
                        "(Deal_Name:equals:" + dealName + ")";

        HttpHeaders headers = new HttpHeaders();

        headers.set(
                "Authorization",
                "Zoho-oauthtoken " + accessToken.getAccess_token()
        );

        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(headers);

        Map<String, Object> body = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
        ).getBody();

        if (body == null || !body.containsKey("data")) {
            return null;
        }

        Object data = body.get("data");

        if (data instanceof List<?> list && !list.isEmpty()) {

            Object firstRecord = list.get(0);

            if (firstRecord instanceof Map<?, ?> map) {

                Object id = map.get("id");

                return id != null ? id.toString() : null;
            }
        }

        return null;
    }

    public String getContactIdByContactName(String contactName)
            throws URISyntaxException {

        var organizationId = Util.getOrgIdFromToken();

        var integrationDetails =
                integrationRepository.findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.ZOHO
                );

        if (integrationDetails == null) {
            return null;
        }

        var accessToken = generateRefreshToken(
                integrationDetails.getRefreshToken(),
                integrationDetails.getPublishableKey()
        );

        var domainExtension =
                getDomainExtension(integrationDetails.getPublishableKey());

        String url =
                "https://www.zohoapis." +
                        domainExtension +
                        "/crm/v2/Contacts/search?criteria=" +
                        "(Full_Name:equals:" + contactName + ")";

        HttpHeaders headers = new HttpHeaders();

        headers.set(
                "Authorization",
                "Zoho-oauthtoken " + accessToken.getAccess_token()
        );

        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(headers);

        Map<String, Object> body = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
        ).getBody();

        if (body == null || !body.containsKey("data")) {
            return null;
        }

        Object data = body.get("data");

        if (data instanceof List<?> list && !list.isEmpty()) {

            Object firstRecord = list.get(0);

            if (firstRecord instanceof Map<?, ?> map) {

                Object id = map.get("id");

                return id != null ? id.toString() : null;
            }
        }

        return null;
    }

    public Map<String, Object> getContactByContactId(String contactId)
            throws URISyntaxException {

        var organizationId = Util.getOrgIdFromToken();

        var integrationDetails =
                integrationRepository.findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.ZOHO
                );

        if (integrationDetails == null) {
            return Collections.emptyMap();
        }

        var accessToken = generateRefreshToken(
                integrationDetails.getRefreshToken(),
                integrationDetails.getPublishableKey()
        );

        var domainExtension =
                getDomainExtension(integrationDetails.getPublishableKey());

        String url =
                "https://www.zohoapis." +
                        domainExtension +
                        "/crm/v2/Contacts/" +
                        contactId;

        HttpHeaders headers = new HttpHeaders();

        headers.set(
                "Authorization",
                "Zoho-oauthtoken " + accessToken.getAccess_token()
        );

        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(headers);

        Map<String, Object> body = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
        ).getBody();

        if (body == null || !body.containsKey("data")) {
            return Collections.emptyMap();
        }

        Object data = body.get("data");

        if (data instanceof List<?> list && !list.isEmpty()) {

            Object firstRecord = list.get(0);

            if (firstRecord instanceof Map<?, ?> map) {

                return (Map<String, Object>) map;
            }
        }

        return Collections.emptyMap();
    }

    public Map<String, Object> getDealByDealId(String dealId)
            throws URISyntaxException {

        var organizationId = Util.getOrgIdFromToken();

        var integrationDetails =
                integrationRepository.findByOrganizationIdAndIntegrationType(
                        organizationId,
                        IntegrationType.ZOHO
                );

        if (integrationDetails == null) {
            return Collections.emptyMap();
        }

        var accessToken = generateRefreshToken(
                integrationDetails.getRefreshToken(),
                integrationDetails.getPublishableKey()
        );

        var domainExtension =
                getDomainExtension(integrationDetails.getPublishableKey());

        String url =
                "https://www.zohoapis." +
                        domainExtension +
                        "/crm/v2/Deals/" +
                        dealId;

        HttpHeaders headers = new HttpHeaders();

        headers.set(
                "Authorization",
                "Zoho-oauthtoken " + accessToken.getAccess_token()
        );

        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(headers);

        Map<String, Object> body = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
        ).getBody();

        if (body == null || !body.containsKey("data")) {
            return Collections.emptyMap();
        }

        Object data = body.get("data");

        if (data instanceof List<?> list && !list.isEmpty()) {

            Object firstRecord = list.get(0);

            if (firstRecord instanceof Map<?, ?> map) {

                return (Map<String, Object>) map;
            }
        }

        return Collections.emptyMap();
    }
}

package com.sharkdom.salesforce.service;

import com.sharkdom.salesforce.dto.SalesforceQueryRequest;
import com.sharkdom.salesforce.dto.SalesforceQueryResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class SalesforceContactService {

    private final RestTemplate restTemplate = new RestTemplate();

    public SalesforceQueryResponse fetchRecentContacts(String instanceUrl, String accessToken, SalesforceQueryRequest queryRequest) {
        String selectedFields = String.join(",", queryRequest.getFields());

        String query = "SELECT " + selectedFields + " FROM Contact ORDER BY LastModifiedDate DESC LIMIT 1000";

        // Don't encode the query, Salesforce expects raw SOQL
        String url = instanceUrl + "/services/data/v59.0/queryAll?q=" + query;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<SalesforceQueryResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                SalesforceQueryResponse.class
        );

        return response.getBody();
    }

    /**
     * Fetch Contact by Contact Id with specific fields
     */
    public Map<String, Object> getContactById(
            String instanceUrl,
            String accessToken,
            String contactId
    ) {

        String url = instanceUrl +
                "/services/data/v60.0/sobjects/Contact/" +
                contactId +
                "?fields=Id,FirstName,LastName,Email,AccountId";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        return response.getBody();
    }

    /**
     * Fetch complete Contact record by Contact Id
     */
    public Map<String, Object> getFullContactById(
            String instanceUrl,
            String accessToken,
            String contactId
    ) {
        String url =
                instanceUrl +
                        "/services/data/v60.0/sobjects/Contact/" +
                        contactId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        return response.getBody();
    }
}


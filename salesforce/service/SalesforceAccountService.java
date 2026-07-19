package com.sharkdom.salesforce.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesforceAccountService {

    private final RestTemplate restTemplate;

    public Map<String,Object> fetchAccounts(
            String instanceUrl,
            String accessToken,
            List<String> fields,
            int limit
    ) {

        log.info("Started fetching Salesforce accounts");
        log.info("Instance URL: {}", instanceUrl);
        log.info("Requested fields: {}", fields);
        log.info("Requested limit: {}", limit);

        validateRequest(fields, limit);

        String selectedFields = String.join(",", fields);

        String soql = "SELECT " + selectedFields +
                " FROM Account ORDER BY LastModifiedDate DESC LIMIT " + limit;

        log.info("Generated SOQL query: {}", soql);

        URI uri = UriComponentsBuilder
                .fromHttpUrl(instanceUrl)
                .path("/services/data/v60.0/query")
                .queryParam("q", soql)
                .encode()
                .build()
                .toUri();

        log.info("Generated Salesforce URI: {}", uri);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {

            log.info("Calling Salesforce Account API");

            ResponseEntity<Map> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            log.info("Salesforce Account API call successful");
            log.info("Response status: {}", response.getStatusCode());

            if (response.getBody() != null) {
                log.debug("Response body: {}", response.getBody());
            }

            return response.getBody();

        } catch (HttpStatusCodeException ex) {

            log.error("Salesforce API failed");
            log.error("Status code: {}", ex.getStatusCode());
            log.error("Error response: {}", ex.getResponseBodyAsString(), ex);

            throw new RuntimeException(
                    "Failed to fetch accounts: " + ex.getResponseBodyAsString()
            );

        } catch (Exception ex) {

            log.error("Unexpected error while fetching Salesforce accounts", ex);

            throw new RuntimeException(
                    "Unexpected error while fetching Salesforce accounts"
            );
        }
    }

    private void validateRequest(List<String> fields, int limit) {

        if (fields == null || fields.isEmpty()) {
            log.error("Fields list cannot be null or empty");
            throw new IllegalArgumentException("Fields cannot be empty");
        }

        if (limit <= 0) {
            log.error("Invalid limit: {}", limit);
            throw new IllegalArgumentException("Limit must be greater than 0");
        }

        log.info("Request validation successful");
    }


    public Map<String, Object> getAccountById(
            String instanceUrl,
            String accessToken,
            String accountId
    ) {
        log.info("Fetching Salesforce account by id={}", accountId);

        String url =
                instanceUrl +
                        "/services/data/v61.0/sobjects/Account/" +
                        accountId;

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
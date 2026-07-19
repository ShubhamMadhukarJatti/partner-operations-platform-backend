package com.sharkdom.salesforce.service;

import com.sharkdom.salesforce.dto.SalesforceQueryRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SalesforceOpportunityService {

    private final RestTemplate restTemplate;

    public Map<String, Object> fetchOpportunities(
            String instanceUrl,
            String accessToken,
            SalesforceQueryRequest queryRequest,
            int limit
    ) {

        String selectedFields = String.join(",", queryRequest.getFields());

        String query = "SELECT " + selectedFields +
                " FROM Opportunity ORDER BY LastModifiedDate DESC LIMIT " + limit;

        String url = instanceUrl + "/services/data/v60.0/query?q=" + query;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        Map.class
                );

        return response.getBody();
    }
}
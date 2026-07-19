package com.sharkdom.salesforce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SalesforceOpportunityDescribeService {

    private final RestTemplate restTemplate;

    public Map<String, Object> describeOpportunity(
            String instanceUrl,
            String accessToken
    ) {

        String url = instanceUrl + "/services/data/v60.0/sobjects/Opportunity/describe";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

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
     * Fetch Opportunity by Opportunity Id
     */
    public Map<String, Object> getOpportunityById(
            String instanceUrl,
            String accessToken,
            String opportunityId
    ) {

        String url = instanceUrl +
                "/services/data/v60.0/sobjects/Opportunity/" +
                opportunityId +
                "?fields=Id,Name,StageName,Amount,AccountId,CloseDate";

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

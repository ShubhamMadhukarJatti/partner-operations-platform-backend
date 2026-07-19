package com.sharkdom.salesforce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SalesforceAccountDescribeService {

    private final RestTemplate restTemplate;

    public Map<String, Object> describeAccount(
            String instanceUrl,
            String accessToken
    ) {

        String url = instanceUrl + "/services/data/v60.0/sobjects/Account/describe";

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
}
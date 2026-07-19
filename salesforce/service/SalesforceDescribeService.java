package com.sharkdom.salesforce.service;

import com.azure.json.implementation.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.salesforce.dto.Field;
import com.sharkdom.salesforce.dto.SalesforceDescribeResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class SalesforceDescribeService {

    private final RestTemplate restTemplate = new RestTemplate();

    public SalesforceDescribeResponse describeContact(String instanceUrl, String accessToken) throws JsonProcessingException, com.fasterxml.jackson.core.JsonProcessingException {
        String url = instanceUrl + "/services/data/v59.0/sobjects/Contact/describe";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                JsonNode.class
        );

        JsonNode fieldsNode = response.getBody().get("fields");

        ObjectMapper mapper = new ObjectMapper();
        List<Field> fields = mapper.readValue(fieldsNode.toString(), new TypeReference<List<Field>>() {});
        return new SalesforceDescribeResponse(fields);
    }


}


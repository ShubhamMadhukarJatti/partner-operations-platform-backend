package com.sharkdom.pipedrive.service;

import com.sharkdom.pipedrive.dto.PersonFieldsResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PipedrivePersonFieldService {

    private final String API_URL = "https://api.pipedrive.com/v1/personFields";

    public PersonFieldsResponse getPersonFields(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken); // "Authorization: Bearer <token>"
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<PersonFieldsResponse> response = restTemplate.exchange(
                API_URL,
                HttpMethod.GET,
                entity,
                PersonFieldsResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to fetch person fields. Status: " + response.getStatusCode());
        }
    }
}
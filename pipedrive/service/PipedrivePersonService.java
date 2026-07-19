package com.sharkdom.pipedrive.service;

import com.sharkdom.pipedrive.dto.PersonsResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PipedrivePersonService {
    private final String API_URL = "https://api.pipedrive.com/v1/persons?limit=10";

    public PersonsResponse getPersons(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<PersonsResponse> response = restTemplate.exchange(
                API_URL,
                HttpMethod.GET,
                entity,
                PersonsResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to fetch persons. Status: " + response.getStatusCode());
        }
    }
}

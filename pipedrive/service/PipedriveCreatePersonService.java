package com.sharkdom.pipedrive.service;

import com.sharkdom.pipedrive.dto.CreatePersonRequest;
import com.sharkdom.pipedrive.dto.CreatePersonResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PipedriveCreatePersonService {
    private final String API_URL = "https://api.pipedrive.com/v1/persons";

    public CreatePersonResponse createPerson(CreatePersonRequest request, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreatePersonRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<CreatePersonResponse> response = restTemplate.exchange(
                API_URL,
                HttpMethod.POST,
                entity,
                CreatePersonResponse.class
        );

        if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to create person. Status: " + response.getStatusCode());
        }
    }
}

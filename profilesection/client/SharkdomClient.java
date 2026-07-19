package com.sharkdom.profilesection.client;

import com.sharkdom.profilesection.config.SharkdomApiConfig;
import com.sharkdom.profilesection.dto.EvaluateApiResponse;
import com.sharkdom.profilesection.dto.EvaluateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class SharkdomClient {

    private final RestTemplate restTemplate;
    private final SharkdomApiConfig config;

    public EvaluateApiResponse evaluate(EvaluateRequest request) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", config.getToken());

        HttpEntity<EvaluateRequest> entity =
                new HttpEntity<>(request, headers);

        ResponseEntity<EvaluateApiResponse> response =
                restTemplate.exchange(
                        config.getUrl(),
                        HttpMethod.POST,
                        entity,
                        EvaluateApiResponse.class
                );

        return response.getBody();
    }
}

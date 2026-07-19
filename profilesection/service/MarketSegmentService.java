package com.sharkdom.profilesection.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.profilesection.dto.PersonaApiRequest;
import com.sharkdom.profilesection.dto.PersonaApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketSegmentService {

    private final RestTemplate restTemplate;

    private static final String PERSONA_API_URL =
            "https://sharkdom-persona-latest.azurewebsites.net/persona";

    public List<String> getMarketSegment(String url) {

        log.info("[CALL PERSONA API] url={}", url);

        try {
            // Request Body
            PersonaApiRequest request = PersonaApiRequest.builder()
                    .urls(List.of(url))
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<PersonaApiRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<List<PersonaApiResponse>> response =
                    restTemplate.exchange(
                            PERSONA_API_URL,
                            HttpMethod.POST,
                            entity,
                            new ParameterizedTypeReference<List<PersonaApiResponse>>() {}
                    );

            List<PersonaApiResponse> body = response.getBody();

            if (body == null || body.isEmpty()) {
                throw new ServiceException(ErrorMessages.SH160, "Empty response from persona API");
            }

            // Extract only marketSegment
            return body.get(0).getMarketSegment();

        } catch (Exception ex) {
            log.error("[PERSONA API ERROR] url={} error={}", url, ex.getMessage(), ex);
            throw new ServiceException(ErrorMessages.SH160, ex.getMessage());
        }
    }
}
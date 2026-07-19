package com.sharkdom.onboarding.service;

import com.sharkdom.dto.AutomationRequestDto;
import com.sharkdom.dto.AutomationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomationDataService {

    private final RestTemplate restTemplate;

    private static final String URL = "https://sharkdom-automation.azurewebsites.net/automation/";

    public AutomationResponseDto triggerAutomation(String website) {

        log.info("Calling Automation API | site={}", website);

        // Request DTO
        AutomationRequestDto requestDto = new AutomationRequestDto(website);

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AutomationRequestDto> requestEntity =
                new HttpEntity<>(requestDto, headers);

        try {
            ResponseEntity<AutomationResponseDto> response =
                    restTemplate.exchange(
                            URL,
                            HttpMethod.POST,
                            requestEntity,
                            AutomationResponseDto.class
                    );

            log.info("Automation API Success");
            return response.getBody();

        } catch (Exception ex) {
            log.error("Automation API failed | site={}", website, ex);
            throw new RuntimeException("Automation API call failed");
        }
    }
}
package com.sharkdom.emailagent.service;

import com.sharkdom.emailagent.dto.EmailRequest;
import com.sharkdom.emailagent.dto.EmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class EmailAgentService {

    private static final String API_URL =
            "https://sharkdomemailagent-fkbtbcaugfcrfxhu.centralindia-01.azurewebsites.net/generate-emails";

    public EmailResponse generateEmails(EmailRequest emailRequest) {
        log.info("Calling Email Agent API for partner: {}", emailRequest.getPartner().getPartner_name());

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EmailRequest> requestEntity = new HttpEntity<>(emailRequest, headers);

        try {
            ResponseEntity<EmailResponse> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    EmailResponse.class
            );

            log.info("API response received successfully with status: {}", response.getStatusCode());
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call Email Agent API: {}", e.getMessage(), e);
            return null;
        }
    }
}
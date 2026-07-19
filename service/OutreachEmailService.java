package com.sharkdom.agenticai.service;

import com.sharkdom.agenticai.model.OutreachEmailRequest;
import com.sharkdom.agenticai.model.OutreachEmailResponse;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutreachEmailService {

    private final RestTemplate restTemplate;

    @Value("${outreach.base-url}")
    private String baseUrl;

    @Value("${outreach.api-key}")
    private String apiKey;

    public OutreachEmailResponse generateEmailOutreach(
            OutreachEmailRequest request) {

        if (request.getORGid() == null || request.getORGname() == null) {
            log.error("Outreach email generation failed. ORGid or ORGname missing");
            throw new ServiceException(ErrorMessages.SH106);
        }

        String url = baseUrl + "/outreach/generate";

        log.info("Calling outreach email API. orgId={}, orgName={}, url={}",
                request.getORGid(),
                request.getORGname(),
                url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", apiKey);

        HttpEntity<OutreachEmailRequest> entity =
                new HttpEntity<>(request, headers);

        try {

            ResponseEntity<OutreachEmailResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            entity,
                            OutreachEmailResponse.class
                    );

            if (!response.getStatusCode().is2xxSuccessful()
                    || response.getBody() == null) {

                log.error("Outreach email API failure. orgId={}, status={}",
                        request.getORGid(),
                        response.getStatusCode());

                throw new ServiceException(
                        ErrorMessages.SH160,
                        "Outreach email API failure"
                );
            }

            log.info("Outreach email generated successfully. orgId={}, subject={}",
                    request.getORGid(),
                    response.getBody().getSubject());

            return response.getBody();

        } catch (RestClientException ex) {

            log.error("Exception while calling outreach email API. orgId={}, error={}",
                    request.getORGid(),
                    ex.getMessage(),
                    ex);

            throw new ServiceException(ErrorMessages.SH160, ex.getMessage());
        }
    }
}
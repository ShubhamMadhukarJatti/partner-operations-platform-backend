package com.sharkdom.agenticai.service;

import com.sharkdom.agenticai.model.OutreachGenerateRequest;
import com.sharkdom.agenticai.model.OutreachGenerateResponse;
import com.sharkdom.agenticai.model.OutreachSchemaResponse;
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
public class OutreachMessageService {

    private final RestTemplate restTemplate;

    @Value("${outreach.base-url}")
    private String baseUrl;

    @Value("${outreach.api-key}")
    private String apiKey;

    public OutreachGenerateResponse generateOutreach(
            OutreachGenerateRequest request) {

        if (request.getORGid() == null) {
            log.error("Outreach generation failed. ORGid is null");
            throw new ServiceException(ErrorMessages.SH106);
        }

        if (request.getORGname() == null || request.getORGname().isBlank()) {
            log.error("Outreach generation failed. ORGname is null or empty");
            throw new ServiceException(ErrorMessages.SH106);
        }

        String url = baseUrl + "/outreach/generate";

        log.info("Calling outreach AI service. orgId={}, orgName={}, url={}",
                request.getORGid(),
                request.getORGname(),
                url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", apiKey);

        HttpEntity<OutreachGenerateRequest> entity =
                new HttpEntity<>(request, headers);

        try {

            ResponseEntity<OutreachGenerateResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            entity,
                            OutreachGenerateResponse.class
                    );

            log.info("Outreach AI API responded. orgId={}, httpStatus={}",
                    request.getORGid(),
                    response.getStatusCode());

            if (!response.getStatusCode().is2xxSuccessful()
                    || response.getBody() == null) {

                log.error("Outreach AI API returned unsuccessful response. orgId={}, httpStatus={}",
                        request.getORGid(),
                        response.getStatusCode());

                throw new ServiceException(
                        ErrorMessages.SH160,
                        "Outreach generation API failure"
                );
            }

            log.info("Outreach message generated successfully. orgId={}", request.getORGid());

            return response.getBody();

        } catch (RestClientException ex) {

            log.error("Exception while calling outreach AI API. orgId={}, error={}",
                    request.getORGid(),
                    ex.getMessage(),
                    ex);

            throw new ServiceException(ErrorMessages.SH160, ex.getMessage());
        }
    }

    public OutreachSchemaResponse getOutreachSchema() {

        String url = baseUrl + "/outreach/schema";

        log.info("Calling outreach schema API. url={}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {

            ResponseEntity<OutreachSchemaResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            OutreachSchemaResponse.class
                    );

            log.info("Outreach schema API responded. httpStatus={}",
                    response.getStatusCode());

            if (!response.getStatusCode().is2xxSuccessful()
                    || response.getBody() == null) {

                log.error("Outreach schema API returned unsuccessful response");

                throw new ServiceException(
                        ErrorMessages.SH160,
                        "Outreach schema API failure"
                );
            }

            return response.getBody();

        } catch (RestClientException ex) {

            log.error("Exception while calling outreach schema API. error={}",
                    ex.getMessage(), ex);

            throw new ServiceException(ErrorMessages.SH160, ex.getMessage());
        }
    }
}
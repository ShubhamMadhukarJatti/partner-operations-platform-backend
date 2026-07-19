package com.sharkdom.agenticai.service;

import com.sharkdom.agenticai.model.ReviewHealthResponse;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewHealthService {

    private final RestTemplate restTemplate;

    @Value("${review.service.base-url}")
    private String baseUrl;

    public ReviewHealthResponse checkHealth() {

        String url = baseUrl + "/health";

        log.info("Calling Sharkdom Review health API. url={}", url);

        try {

            ResponseEntity<ReviewHealthResponse> response =
                    restTemplate.getForEntity(url, ReviewHealthResponse.class);

            log.info("Review health API responded. httpStatus={}",
                    response.getStatusCode());

            if (!response.getStatusCode().is2xxSuccessful()
                    || response.getBody() == null) {

                log.error("Review health API returned invalid response. httpStatus={}",
                        response.getStatusCode());

                throw new ServiceException(
                        ErrorMessages.SH160,
                        "Review health API failure"
                );
            }

            ReviewHealthResponse body = response.getBody();

            mapStatus(body);

            log.info("Review health status fetched successfully. service={}, status={}",
                    body.getService(), body.getStatus());

            return body;

        } catch (RestClientException ex) {

            log.error("Exception while calling Review health API. error={}",
                    ex.getMessage(), ex);

            throw new ServiceException(ErrorMessages.SH160, ex.getMessage());
        }
    }

    private void mapStatus(ReviewHealthResponse response) {

        if ("ok".equalsIgnoreCase(response.getStatus())) {
            response.setDisplayStatus("SERVICE_RUNNING");
        } else {
            response.setDisplayStatus("SERVICE_DOWN");
        }
    }
}
package com.sharkdom.agenticai.service;

import com.sharkdom.agenticai.model.ReviewFetchRequest;
import com.sharkdom.agenticai.model.ReviewFetchResponse;
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
public class ReviewFetchService {

    private final RestTemplate restTemplate;

    @Value("${review.service.base-url}")
    private String baseUrl;

    public ReviewFetchResponse fetchReviews(ReviewFetchRequest request) {

        String url = baseUrl + "/reviews";

        log.info("Calling Review fetch API. orgName={}, url={}",
                request.getOrgName(), url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ReviewFetchRequest> entity =
                new HttpEntity<>(request, headers);

        try {

            ResponseEntity<ReviewFetchResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            entity,
                            ReviewFetchResponse.class
                    );

            log.info("Review API responded. httpStatus={}",
                    response.getStatusCode());

            if (!response.getStatusCode().is2xxSuccessful()
                    || response.getBody() == null) {

                throw new ServiceException(
                        ErrorMessages.SH160,
                        "Review fetch API failure"
                );
            }

            ReviewFetchResponse body = response.getBody();

            mapResponse(body);

            log.info("Review fetch completed. org={}, success={}",
                    request.getOrgName(), body.getSuccess());

            return body;

        } catch (RestClientException ex) {

            log.error("Exception while calling review API. error={}",
                    ex.getMessage(), ex);

            throw new ServiceException(ErrorMessages.SH160, ex.getMessage());
        }
    }

    private void mapResponse(ReviewFetchResponse response) {

        if (response.getError() != null) {

            if (response.getError().contains("Validation")) {
                response.setDisplayMessage("Invalid request. Please check organization URL.");
            } else if (response.getError().contains("timed out")) {
                response.setDisplayMessage("Review fetch timed out. Please try again.");
            } else {
                response.setDisplayMessage("Review service error.");
            }

            return;
        }

        if (Boolean.TRUE.equals(response.getSuccess())) {

            if (response.getData() != null
                    && response.getData().getTrustpilot() != null
                    && Boolean.TRUE.equals(response.getData().getTrustpilot().getFound())) {

                response.setDisplayMessage("Reviews fetched successfully");

            } else {

                response.setDisplayMessage("No Trustpilot reviews found for this organization");
            }

        } else {

            response.setDisplayMessage("Review fetch completed with warnings");
        }
    }
}
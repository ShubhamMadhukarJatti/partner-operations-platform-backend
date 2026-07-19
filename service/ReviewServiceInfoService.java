package com.sharkdom.agenticai.service;

import com.sharkdom.agenticai.model.ReviewServiceInfoResponse;
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
public class ReviewServiceInfoService {

    private final RestTemplate restTemplate;

    @Value("${review.service.base-url}")
    private String baseUrl;

    public ReviewServiceInfoResponse getServiceInfo() {

        String url = baseUrl + "/";

        log.info("Calling Sharkdom Review service info API. url={}", url);

        try {

            ResponseEntity<ReviewServiceInfoResponse> response =
                    restTemplate.getForEntity(url, ReviewServiceInfoResponse.class);

            log.info("Review service info API responded. httpStatus={}",
                    response.getStatusCode());

            if (!response.getStatusCode().is2xxSuccessful()
                    || response.getBody() == null) {

                log.error("Review service info API returned invalid response. httpStatus={}",
                        response.getStatusCode());

                throw new ServiceException(
                        ErrorMessages.SH160,
                        "Review service info API failure"
                );
            }

            ReviewServiceInfoResponse body = response.getBody();

            body.setDisplayStatus("SERVICE_AVAILABLE");

            log.info("Review service info fetched successfully. service={}, version={}",
                    body.getService(), body.getVersion());

            return body;

        } catch (RestClientException ex) {

            log.error("Exception while calling Review service info API. error={}",
                    ex.getMessage(), ex);

            throw new ServiceException(ErrorMessages.SH160, ex.getMessage());
        }
    }
}
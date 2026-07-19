package com.sharkdom.partnerattribution.emails.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.partnerattribution.emails.dto.IntroGenerateRequest;
import com.sharkdom.partnerattribution.emails.dto.response.IntroGenerateResponse;
import com.sharkdom.partnerattribution.emails.service.IntroService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntroServiceImpl implements IntroService {

    private final RestTemplate restTemplate;

    private static final String EXTERNAL_API_URL =
            "https://sharkdom-intro-api-gfbkdeaqega4hkbj.centralindia-01.azurewebsites.net/generate";

    @Override
    public IntroGenerateResponse generateIntro(IntroGenerateRequest request) {

        log.info("Started intro generation");

        validateRequest(request);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<IntroGenerateRequest> entity =
                    new HttpEntity<>(request, headers);

            IntroGenerateResponse response = restTemplate.postForObject(
                    EXTERNAL_API_URL,
                    entity,
                    IntroGenerateResponse.class
            );

            if (Objects.isNull(response)) {
                log.error("External API returned null response");
                throw new ServiceException(ErrorMessages.SH160, "Null response from intro API");
            }

            log.info("Intro generated successfully for type: {}", request.getType());

            return response;

        } catch (ServiceException ex) {
            log.error("Service exception: {}", ex.getMessage());
            throw ex;

        } catch (Exception ex) {
            log.error("Error while calling external intro API", ex);
            throw new ServiceException(ErrorMessages.SH116, ex.getMessage());
        }
    }

    private void validateRequest(IntroGenerateRequest request) {

        if (Objects.isNull(request)) {
            throw new ServiceException(ErrorMessages.SH05);
        }

        if (Objects.isNull(request.getType())) {
            throw new ServiceException(ErrorMessages.SH106);
        }

        if (Objects.isNull(request.getData())) {
            throw new ServiceException(ErrorMessages.SH106);
        }
    }
}
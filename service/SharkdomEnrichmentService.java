package com.sharkdom.partnerattribution.service;

import com.sharkdom.partnerattribution.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

/**
 * Service for interacting with Sharkdom Enrichment APIs.
 *
 * <p>
 * Handles:
 * - Health Check API
 * - Service Info API
 * </p>
 */
@Slf4j
@Service
public class SharkdomEnrichmentService {

    private static final String BASE_URL =
            "https://sharkdom-enrichment-ayb9b6f6heezbkdq.canadacentral-01.azurewebsites.net";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Calls /health API of enrichment service.
     *
     * @return health response DTO
     */
    public SharkdomEnrichmentHealthResponse getHealth() {

        String url = BASE_URL + "/health";

        log.info("Calling Enrichment Health API | url={}", url);

        try {

            ResponseEntity<SharkdomEnrichmentHealthResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            SharkdomEnrichmentHealthResponse.class
                    );

            log.info("Enrichment Health API Success | status={}", response.getStatusCode());

            return response.getBody();

        } catch (HttpClientErrorException e) {

            log.error("Client Error while calling Enrichment Health API | status={} | body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            throw new RuntimeException("Client error while calling enrichment health API");

        } catch (HttpServerErrorException e) {

            log.error("Server Error while calling Enrichment Health API | status={} | body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            throw new RuntimeException("Server error while calling enrichment health API");

        } catch (Exception e) {

            log.error("Unexpected error while calling Enrichment Health API", e);

            throw new RuntimeException("Unexpected error while calling enrichment health API");
        }
    }

    /**
     * Calls root (/) API to fetch service metadata.
     *
     * @return service info DTO
     */
    public SharkdomEnrichmentInfoResponse getServiceInfo() {

        String url = BASE_URL + "/";

        log.info("Calling Enrichment Info API | url={}", url);

        try {

            ResponseEntity<SharkdomEnrichmentInfoResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            SharkdomEnrichmentInfoResponse.class
                    );

            log.info("Enrichment Info API Success | status={}", response.getStatusCode());

            return response.getBody();

        } catch (HttpClientErrorException e) {

            log.error("Client Error while calling Enrichment Info API | status={} | body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            throw new RuntimeException("Client error while calling enrichment info API");

        } catch (HttpServerErrorException e) {

            log.error("Server Error while calling Enrichment Info API | status={} | body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            throw new RuntimeException("Server error while calling enrichment info API");

        } catch (Exception e) {

            log.error("Unexpected error while calling Enrichment Info API", e);

            throw new RuntimeException("Unexpected error while calling enrichment info API");
        }
    }

    /**
     * Calls enrichment API to fetch decision makers.
     */
    public EnrichmentResponse enrich(EnrichmentRequest request) {

        String url = BASE_URL + "/enrich";

        log.info("Calling Enrichment API | orgName={} | departments={}",
                request.getOrgName(), request.getDepartments());

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<EnrichmentRequest> entity =
                    new HttpEntity<>(request, headers);

            ResponseEntity<EnrichmentResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            entity,
                            EnrichmentResponse.class
                    );

            log.info("Enrichment API Success | status={}", response.getStatusCode());

            return response.getBody();

        } catch (HttpClientErrorException e) {

            log.error("Client Error | status={} | body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            throw new RuntimeException("Client error while calling enrichment API");

        } catch (HttpServerErrorException e) {

            log.error("Server Error | status={} | body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            throw new RuntimeException("Server error while calling enrichment API");

        } catch (Exception e) {

            log.error("Unexpected error while calling enrichment API", e);

            throw new RuntimeException("Unexpected error while calling enrichment API");
        }
    }

    /**
     * Fetch available departments for enrichment.
     */
    public DepartmentsResponse getDepartments() {

        String url = BASE_URL + "/departments";

        log.info("Calling Enrichment Departments API");

        try {

            ResponseEntity<DepartmentsResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            DepartmentsResponse.class
                    );

            log.info("Departments API Success | status={}", response.getStatusCode());

            return response.getBody();

        } catch (HttpClientErrorException e) {

            log.error("Client Error | status={} | body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            throw new RuntimeException("Client error while fetching departments");

        } catch (HttpServerErrorException e) {

            log.error("Server Error | status={} | body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());

            throw new RuntimeException("Server error while fetching departments");

        } catch (Exception e) {

            log.error("Unexpected error while fetching departments", e);

            throw new RuntimeException("Unexpected error while fetching departments");
        }
    }
}

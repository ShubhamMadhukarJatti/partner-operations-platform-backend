package com.sharkdom.dataenrichment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataEnrichmentService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${data.enrichment.base-url}")
    private String baseUrl;

    private static final String FIND_EMPLOYEES_API =
            "/api/find-employees?no_cache=false";

    private static final String FIND_PERSON_API =
            "/api/find-person?no_cache=false";

    /**
     * ==========================================
     * FIND EMPLOYEES API
     * ==========================================
     */
    public DataEnrichmentResponse findEmployees(
            DataEnrichmentRequest request
    ) {

        long startTime = System.currentTimeMillis();

        log.info("""
                
                ==========================================
                DATA ENRICHMENT - FIND EMPLOYEES STARTED
                ==========================================
                Company       : {}
                Designation   : {}
                Location      : {}
                Limit         : {}
                StrictCurrent : {}
                UserId        : {}
                ==========================================
                """,
                request.getCompany(),
                request.getDesignation(),
                request.getLocation(),
                request.getLimit(),
                request.getStrictCurrent(),
                request.getUserId()
        );

        try {

            String url = baseUrl + FIND_EMPLOYEES_API;

            HttpEntity<DataEnrichmentRequest> entity =
                    new HttpEntity<>(request, getHeaders());

            log.info("Calling Find Employees API : {}", url);

            log.debug("Find Employees Request Payload : {}", toJson(request));

            ResponseEntity<DataEnrichmentResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            entity,
                            DataEnrichmentResponse.class
                    );

            long totalTime = System.currentTimeMillis() - startTime;

            log.info("""
                    
                    ==========================================
                    FIND EMPLOYEES SUCCESS
                    ==========================================
                    Status Code   : {}
                    Total Fetched : {}
                    Total Matched : {}
                    Cost USD      : {}
                    Cached        : {}
                    Time Taken    : {} ms
                    ==========================================
                    """,
                    response.getStatusCode(),
                    response.getBody() != null
                            ? response.getBody().getTotalFetched()
                            : 0,
                    response.getBody() != null
                            ? response.getBody().getTotalMatched()
                            : 0,
                    response.getBody() != null
                            ? response.getBody().getCostUsd()
                            : 0,
                    response.getBody() != null
                            ? response.getBody().getCached()
                            : false,
                    totalTime
            );

            return response.getBody();

        } catch (HttpStatusCodeException ex) {

            handleHttpException(
                    "FIND EMPLOYEES API ERROR",
                    ex
            );

            throw new DataEnrichmentException(
                    "Failed to fetch employees from enrichment API"
            );

        } catch (Exception ex) {

            handleGenericException(
                    "FIND EMPLOYEES UNEXPECTED ERROR",
                    ex
            );

            throw new DataEnrichmentException(
                    "Unexpected error occurred while fetching employees"
            );
        }
    }

    /**
     * ==========================================
     * FIND PERSON API
     * ==========================================
     */
    public FindPersonResponse findPerson(
            FindPersonRequest request
    ) {

        long startTime = System.currentTimeMillis();

        log.info("""
                
                ==========================================
                DATA ENRICHMENT - FIND PERSON STARTED
                ==========================================
                Name            : {}
                Company         : {}
                Location        : {}
                Title           : {}
                Education       : {}
                Include Emails  : {}
                Limit           : {}
                UserId          : {}
                ==========================================
                """,
                request.getName(),
                request.getCompany(),
                request.getLocation(),
                request.getTitle(),
                request.getEducation(),
                request.getIncludeEmails(),
                request.getLimit(),
                request.getUserId()
        );

        try {

            String url = baseUrl + FIND_PERSON_API;

            HttpEntity<FindPersonRequest> entity =
                    new HttpEntity<>(request, getHeaders());

            log.info("Calling Find Person API : {}", url);

            log.debug("Find Person Request Payload : {}", toJson(request));

            ResponseEntity<FindPersonResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            entity,
                            FindPersonResponse.class
                    );

            long totalTime = System.currentTimeMillis() - startTime;

            log.info("""
                    
                    ==========================================
                    FIND PERSON SUCCESS
                    ==========================================
                    Status Code            : {}
                    Total Candidates Found : {}
                    Total Scraped          : {}
                    Cost USD               : {}
                    Cached                 : {}
                    Time Taken             : {} ms
                    ==========================================
                    """,
                    response.getStatusCode(),
                    response.getBody() != null
                            ? response.getBody().getTotalCandidatesFound()
                            : 0,
                    response.getBody() != null
                            ? response.getBody().getTotalScraped()
                            : 0,
                    response.getBody() != null
                            ? response.getBody().getCostUsd()
                            : 0,
                    response.getBody() != null
                            ? response.getBody().getCached()
                            : false,
                    totalTime
            );

            return response.getBody();

        } catch (HttpStatusCodeException ex) {

            handleHttpException(
                    "FIND PERSON API ERROR",
                    ex
            );

            throw new DataEnrichmentException(
                    "Failed to find person from enrichment API"
            );

        } catch (Exception ex) {

            handleGenericException(
                    "FIND PERSON UNEXPECTED ERROR",
                    ex
            );

            throw new DataEnrichmentException(
                    "Unexpected error occurred while finding person"
            );
        }
    }

    /**
     * ==========================================
     * COMMON HEADERS
     * ==========================================
     */
    private HttpHeaders getHeaders() {

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.setAccept(
                List.of(MediaType.APPLICATION_JSON)
        );

        return headers;
    }

    /**
     * ==========================================
     * HANDLE HTTP EXCEPTION
     * ==========================================
     */
    private void handleHttpException(
            String title,
            HttpStatusCodeException ex
    ) {

        log.error("""
                
                ==========================================
                {}
                ==========================================
                Status Code : {}
                Response    : {}
                ==========================================
                """,
                title,
                ex.getStatusCode(),
                ex.getResponseBodyAsString(),
                ex
        );
    }

    /**
     * ==========================================
     * HANDLE GENERIC EXCEPTION
     * ==========================================
     */
    private void handleGenericException(
            String title,
            Exception ex
    ) {

        log.error("""
                
                ==========================================
                {}
                ==========================================
                Message : {}
                ==========================================
                """,
                title,
                ex.getMessage(),
                ex
        );
    }

    /**
     * ==========================================
     * OBJECT TO JSON
     * ==========================================
     */
    private String toJson(Object object) {

        try {

            return objectMapper.writeValueAsString(object);

        } catch (JsonProcessingException e) {

            return "Unable to convert object to JSON";
        }
    }

    private static final String HEALTH_CHECK_API =
            "/api/healthz";

    /**
     * ==========================================
     * HEALTH CHECK API
     * ==========================================
     */
    public HealthCheckResponse healthCheck() {

        long startTime = System.currentTimeMillis();

        log.info("""
            
            ==========================================
            DATA ENRICHMENT HEALTH CHECK STARTED
            ==========================================
            """);

        try {

            String url = baseUrl + HEALTH_CHECK_API;

            HttpHeaders headers = new HttpHeaders();

            headers.setAccept(
                    List.of(MediaType.APPLICATION_JSON)
            );

            HttpEntity<Void> entity =
                    new HttpEntity<>(headers);

            log.info("Calling Health Check API : {}", url);

            ResponseEntity<HealthCheckResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            HealthCheckResponse.class
                    );

            long totalTime = System.currentTimeMillis() - startTime;

            log.info("""
                
                ==========================================
                HEALTH CHECK SUCCESS
                ==========================================
                Status Code : {}
                Status      : {}
                Time Taken  : {} ms
                ==========================================
                """,
                    response.getStatusCode(),
                    response.getBody() != null
                            ? response.getBody().getStatus()
                            : "UNKNOWN",
                    totalTime
            );

            return response.getBody();

        } catch (HttpStatusCodeException ex) {

            handleHttpException(
                    "HEALTH CHECK API ERROR",
                    ex
            );

            throw new DataEnrichmentException(
                    "Failed to call health check API"
            );

        } catch (Exception ex) {

            handleGenericException(
                    "HEALTH CHECK UNEXPECTED ERROR",
                    ex
            );

            throw new DataEnrichmentException(
                    "Unexpected error occurred during health check"
            );
        }
    }
}
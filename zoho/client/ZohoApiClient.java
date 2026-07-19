package com.sharkdom.zoho.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ZohoApiClient {

    private final RestTemplate restTemplate;

    public Map<String, Object> getRecord(

            String apiDomain,

            String accessToken,

            String module,

            String recordId

    ) {

        try {

            String url =
                    apiDomain
                            + "/crm/v8/"
                            + module
                            + "/"
                            + recordId;

            log.info(
                    "Preparing Zoho API Request | Url={} | Module={} | RecordId={}",
                    url,
                    module,
                    recordId
            );

            log.info(
                    "Access Token : {}",
                    accessToken
            );

            HttpHeaders headers =
                    new HttpHeaders();

            headers.set(
                    "Authorization",
                    "Zoho-oauthtoken "
                            + accessToken
            );

            headers.setContentType(
                    MediaType.APPLICATION_JSON
            );

            log.info(
                    "Request Headers : {}",
                    headers
            );

            HttpEntity<Void> entity =
                    new HttpEntity<>(headers);

            log.info(
                    "Calling Zoho API"
            );

            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            Map.class
                    );

            log.info(
                    "Zoho API Response Status : {}",
                    response.getStatusCode()
            );

            log.info(
                    "Zoho API Response Body : {}",
                    response.getBody()
            );

            return response.getBody();

        } catch (HttpStatusCodeException e) {

            log.error(
                    "Zoho API Error | Status={} | Response={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );

            throw new RuntimeException(
                    "Failed to fetch Zoho record"
            );

        } catch (Exception e) {

            log.error(
                    "Unexpected Error Fetching Zoho Record",
                    e
            );

            throw new RuntimeException(
                    "Failed to fetch Zoho record"
            );
        }
    }
}
package com.sharkdom.service.organization;

import com.sharkdom.dto.SearchRequest;
import com.sharkdom.dto.SearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class SearchService {

    private final RestTemplate restTemplate;

    private String searchUrl="https://sharkdom-search-cdd5a2h0ftgbfqah.centralindia-01.azurewebsites.net";

    public SearchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public SearchResponse searchCompanies(String sector, String input) {
        String url = searchUrl + "/query";

        SearchRequest request = new SearchRequest(sector, input);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SearchRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<SearchResponse> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, SearchResponse.class);

            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            log.error("Error while calling search API: {}", ex.getResponseBodyAsString(), ex);
            throw new RuntimeException("Search API failed: " + ex.getMessage());
        }
    }
}

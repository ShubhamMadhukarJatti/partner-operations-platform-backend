package com.sharkdom.client.trello;

import com.sharkdom.model.trello.TrelloAuthParams;
import com.sharkdom.model.trello.client.TrelloBoardResponse;
import com.sharkdom.model.trello.client.TrelloCardResponse;
import com.sharkdom.model.trello.client.TrelloListResponse;
import com.sharkdom.model.trello.client.TrelloMemberResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrelloClient {

    private final RestTemplate restTemplate;
    private final TrelloAuthParams authParams;

    public TrelloCardResponse createCardForList(String idList, String name, String desc) {
        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Build form data
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("key", authParams.getKey());
        formData.add("token", authParams.getToken());
        formData.add("idList", idList);
        formData.add("name", name);
        formData.add("desc", desc);

        // Create request entity
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        String url = authParams.getBaseUrl() + "/cards";
        log.info("URL to create card for list : {}", url);
        try {
            TrelloCardResponse trelloCardResponse = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<TrelloCardResponse>() {
                    }
            ).getBody();
            log.info("Successfully created the card for list. Response: {}", trelloCardResponse);
            return trelloCardResponse;
        } catch (RestClientResponseException e) {
            log.error("Failed to create card for list. Response: {}: {}", e.getStatusCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred while creating card for list {}", e.getMessage());
            throw e;
        }
    }

    public List<TrelloCardResponse> fetchCardForList(String listId) {
        String url = buildUrl("/lists/{listId}/cards", null, listId);
        log.info("URL to fetch card for list : {}", url);
        try {
            List<TrelloCardResponse> trelloCardResponses = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<TrelloCardResponse>>() {
                    }
            ).getBody();
            log.info("Successfully fetched the card for list. Response: {}", trelloCardResponses);
            return trelloCardResponses;
        } catch (RestClientResponseException e) {
            log.error("Failed to fetch cards for list. Response: {}: {}", e.getStatusCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred while fetching cards for list {}", e.getMessage());
            throw e;
        }
    }

    public List<TrelloListResponse> fetchListForBoard(String boardId) {
        String url = buildUrl("/boards/{boardId}/lists", null, boardId);
        log.info("URL to fetch list for board : {}", url);
        try {
            List<TrelloListResponse> trelloListResponses = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<TrelloListResponse>>() {
                    }
            ).getBody();
            log.info("Successfully fetched the list for board. Response: {}", trelloListResponses);
            return trelloListResponses;
        } catch (RestClientResponseException e) {
            log.error("Failed to fetch lists for board. Response: {}: {}", e.getStatusCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred while fetching lists for board {}", e.getMessage());
            throw e;
        }
    }

    public List<TrelloBoardResponse> fetchAllBoard(String memberId) {
        String setMemberId = ObjectUtils.isEmpty(memberId) ? "me" : memberId;
        String path = "/members/" + setMemberId + "/boards";
        String url = buildUrl(path, null);
        log.info("URL to fetch all board : {}", url);
        try {
            List<TrelloBoardResponse> trelloBoardResponses = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<TrelloBoardResponse>>() {
                    }
            ).getBody();
            log.info("Successfully fetched all board. Response: {}", trelloBoardResponses);
            return trelloBoardResponses;
        } catch (RestClientResponseException e) {
            log.error("Failed to fetch board for the current member. Response: {}: {}", e.getStatusCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred while fetching board for the current member {}", e.getMessage());
            throw e;
        }
    }

    public TrelloMemberResponse fetchMember(String memberId) {
        String setMemberId = ObjectUtils.isEmpty(memberId) ? "me" : memberId;
        String path = "/members/" + setMemberId;
        String url = buildUrl(path, null);
        log.info("URL to fetch Member : {}", url);
        try {
            TrelloMemberResponse trelloMemberResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<TrelloMemberResponse>() {
                    }
            ).getBody();
            log.info("Successfully fetched the members. Response: {}", trelloMemberResponse);
            return trelloMemberResponse;
        } catch (RestClientResponseException e) {
            log.error("Failed to fetch the current member. Response: {}: {}", e.getStatusCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("An unexpected error occurred while fetching the current member {}", e.getMessage());
            throw e;
        }
    }



    private String buildUrl(String path, Map<String, String> queryParams, String... pathVariables) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(authParams.getBaseUrl() + path)
                .queryParam("key", authParams.getKey())
                .queryParam("token", authParams.getToken());
        if (queryParams != null) {
            queryParams.forEach(builder::queryParam);
        }
        return pathVariables.length > 0
                ? builder.buildAndExpand((Object[]) pathVariables).toUriString()
                : builder.toUriString();
    }
}

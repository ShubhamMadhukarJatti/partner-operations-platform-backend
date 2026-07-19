package com.sharkdom.gtm.service.trello;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TrelloBoardService {

    @Value("${trello.api.base-url:https://api.trello.com/1}")
    private String trelloBaseUrl;

    @Value("${trello.api.key}")
    private String trelloApiKey;

    @Autowired
    private IntegrationRepository integrationRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SharkdomApiResponse<List<Map<String, Object>>> getUserBoards(String token) {
        long startTime = System.currentTimeMillis();
        String url = String.format("%s/members/me/boards?key=%s&token=%s", trelloBaseUrl, trelloApiKey, token);

        log.info("[TrelloService] Request to fetch Trello boards started.");
        log.debug("[TrelloService] Request URL: {}", url.replace(token, "****")); // mask token

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            long duration = System.currentTimeMillis() - startTime;
            log.info("[TrelloService] Trello API responded in {} ms with status {}", duration, response.getStatusCodeValue());

            JsonNode root = objectMapper.readTree(response.getBody());
            List<Map<String, Object>> boards = objectMapper.convertValue(root, new TypeReference<>() {});

            if (boards.isEmpty()) {
                log.warn("[TrelloService] No boards found for the provided Trello token.");
                return new SharkdomApiResponse<>(true, "No Trello boards found for this account.", boards);
            }

            for (Map<String, Object> board : boards) {
                log.info("[TrelloService] Board found: name='{}', id='{}', url='{}'",
                        board.get("name"), board.get("id"), board.get("url"));
            }

            return new SharkdomApiResponse<>(true, "Trello boards fetched successfully.", boards);

        } catch (Exception e) {
            log.error("[TrelloService] Error fetching Trello boards: {}", e.getMessage(), e);
            return new SharkdomApiResponse<>(false, "Failed to fetch Trello boards: " + e.getMessage(), null);
        }
    }

    public SharkdomApiResponse<Map<String, Object>> getBoardById(String boardId, String token) {
        long startTime = System.currentTimeMillis();
        String url = String.format("%s/boards/%s?key=%s&token=%s", trelloBaseUrl, boardId, trelloApiKey, token);

        log.info("[TrelloService] Request to fetch Trello board details started for boardId={}", boardId);
        log.debug("[TrelloService] Request URL: {}", url.replace(token, "****")); // mask token

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            long duration = System.currentTimeMillis() - startTime;
            log.info("[TrelloService] Trello board fetch completed in {} ms with status {}", duration, response.getStatusCodeValue());

            // Parse board details
            Map<String, Object> board = objectMapper.readValue(response.getBody(), new TypeReference<>() {});

            if (board == null || board.isEmpty()) {
                log.warn("[TrelloService] No board details found for boardId={}", boardId);
                return new SharkdomApiResponse<>(true, "No Trello board found with this ID.", null);
            }

            log.info("[TrelloService] Board Details: name='{}', id='{}', url='{}'",
                    board.get("name"), board.get("id"), board.get("url"));

            return new SharkdomApiResponse<>(true, "Trello board details fetched successfully.", board);

        } catch (Exception e) {
            log.error("[TrelloService] Failed to fetch Trello board details for boardId={}: {}", boardId, e.getMessage(), e);
            return new SharkdomApiResponse<>(false, "Error while fetching Trello board details: " + e.getMessage(), null);
        }
    }


    /**
     * Fetch Trello boards for the current organization based on saved integration details.
     */
    public SharkdomApiResponse<List<Map<String, Object>>> getUserBoards() {
        Long organizationId = Util.getOrgIdFromToken();
        log.info("[TrelloService] Fetching Trello integration details for organization ID: {}", organizationId);

        IntegrationDetails integrationDetails =
                integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.TRELLO);

        if (integrationDetails == null) {
            log.warn("[TrelloService] No Trello integration found for organization ID: {}", organizationId);
            return new SharkdomApiResponse<>(false,
                    "No Trello integration found for this organization. Please connect Trello first.",
                    null);
        }

        if (!integrationDetails.isConnected()) {
            log.warn("[TrelloService] Trello integration found but not connected for organization ID: {}", organizationId);
            return new SharkdomApiResponse<>(false,
                    "Trello integration is disconnected. Please reconnect Trello.",
                    null);
        }

        if (integrationDetails.getRefreshToken() == null) {
            log.warn("[TrelloService] Trello integration missing token for organization ID: {}", organizationId);
            return new SharkdomApiResponse<>(false,
                    "Trello integration token not found. Please reconnect Trello.",
                    null);
        }

        log.info("[TrelloService] Valid Trello integration found for organization ID: {}. Fetching boards...", organizationId);
        return getUserBoards(integrationDetails.getRefreshToken());
    }

    public SharkdomApiResponse<Map<String, Object>> getBoardById(String boardId) {
        Long organizationId = Util.getOrgIdFromToken();
        log.info("[TrelloService] Fetching Trello integration details for organization ID: {}", organizationId);

        IntegrationDetails integrationDetails =
                integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.TRELLO);

        if (integrationDetails == null) {
            log.warn("[TrelloService] No Trello integration found for organization ID: {}", organizationId);
            return new SharkdomApiResponse<>(false,
                    "No Trello integration found for this organization. Please connect Trello first.",
                    null);
        }

        if (!integrationDetails.isConnected()) {
            log.warn("[TrelloService] Trello integration found but not connected for organization ID: {}", organizationId);
            return new SharkdomApiResponse<>(false,
                    "Trello integration is disconnected. Please reconnect Trello.",
                    null);
        }

        if (integrationDetails.getRefreshToken() == null) {
            log.warn("[TrelloService] Trello integration missing token for organization ID: {}", organizationId);
            return new SharkdomApiResponse<>(false,
                    "Trello integration token not found. Please reconnect Trello.",
                    null);
        }

        log.info("[TrelloService] Valid Trello integration found for organization ID: {}. Fetching boardId={}",
                organizationId, boardId);

        // Delegate to existing method
        return getBoardById(boardId, integrationDetails.getRefreshToken());
    }

    public SharkdomApiResponse<Object> getCardDetails(String cardId) {
        Long organizationId = Util.getOrgIdFromToken();
        log.info("[TrelloService] Fetching card details for orgId={}, cardId={}", organizationId, cardId);

        try {
            // Check integration
            IntegrationDetails integrationDetails = integrationRepository.findByOrganizationIdAndIntegrationType(
                    organizationId, IntegrationType.TRELLO);

            if (integrationDetails == null) {
                log.warn("[TrelloService] Trello integration not found for organization ID: {}", organizationId);
                return new SharkdomApiResponse<>(false, "Trello integration not found for this organization.", null);
            }

            if (!integrationDetails.isConnected() || integrationDetails.getRefreshToken() == null) {
                log.warn("[TrelloService] Trello integration is disconnected or token missing for organization ID: {}", organizationId);
                return new SharkdomApiResponse<>(false, "Trello integration is disconnected. Please reconnect Trello.", null);
            }

            // Build URL
            String token = integrationDetails.getRefreshToken();
            String url = String.format("%s/cards/%s?key=%s&token=%s", trelloBaseUrl, cardId, trelloApiKey, token);

            log.debug("[TrelloService] Card fetch URL: {}", url.replace(token, "****")); // mask token in logs

            // Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            long startTime = System.currentTimeMillis();

            // Execute
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            long duration = System.currentTimeMillis() - startTime;

            log.info("[TrelloService] Card details fetched successfully in {} ms", duration);
            log.debug("[TrelloService] Raw Card Response: {}", response.getBody());

            JsonNode cardData = objectMapper.readTree(response.getBody());
            return new SharkdomApiResponse<>(true, "Trello card details fetched successfully", cardData);

        } catch (Exception e) {
            log.error("[TrelloService] Error fetching Trello card details: {}", e.getMessage(), e);
            return new SharkdomApiResponse<>(false, "Failed to fetch Trello card details: " + e.getMessage(), null);
        }
    }


    public SharkdomApiResponse<Object> createCard(String listId, String name, String description) {
        Long organizationId = Util.getOrgIdFromToken();
        log.info("[TrelloService] Request to create Trello card in listId={} under orgId={}", listId, organizationId);

        try {
            IntegrationDetails integrationDetails =
                    integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.TRELLO);

            if (integrationDetails == null) {
                log.warn("[TrelloService] Trello integration not found for organization ID: {}", organizationId);
                return new SharkdomApiResponse<>(false, "Trello integration not found for this organization.", null);
            }

            if (!integrationDetails.isConnected() || integrationDetails.getRefreshToken() == null) {
                log.warn("[TrelloService] Trello integration is disconnected or token missing for organization ID: {}", organizationId);
                return new SharkdomApiResponse<>(false, "Trello integration is disconnected. Please reconnect Trello.", null);
            }

            String token = integrationDetails.getRefreshToken();
            String url = String.format("%s/cards?key=%s&token=%s", trelloBaseUrl, trelloApiKey, token);

            log.debug("[TrelloService] Trello Create Card URL: {}", url.replace(token, "****")); // mask token

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Prepare request body
            String requestBody = String.format("idList=%s&name=%s&desc=%s",
                    listId, name, description != null ? description : "");

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            long startTime = System.currentTimeMillis();

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            long duration = System.currentTimeMillis() - startTime;

            log.info("[TrelloService] Trello card created successfully in {} ms", duration);
            log.debug("[TrelloService] Raw Card Creation Response: {}", response.getBody());

            JsonNode cardData = objectMapper.readTree(response.getBody());

            return new SharkdomApiResponse<>(true, "Trello card created successfully", cardData);

        } catch (Exception e) {
            log.error("[TrelloService] Error while creating Trello card: {}", e.getMessage(), e);
            return new SharkdomApiResponse<>(false, "Failed to create Trello card: " + e.getMessage(), null);
        }
    }

    public SharkdomApiResponse<Object> createBoard(String boardName) {
        Long organizationId = Util.getOrgIdFromToken();
        log.info("[TrelloService] Request to create Trello board '{}' for organization ID {}", boardName, organizationId);

        try {
            IntegrationDetails integrationDetails =
                    integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.TRELLO);

            if (integrationDetails == null) {
                log.warn("[TrelloService] Trello integration not found for organization ID: {}", organizationId);
                return new SharkdomApiResponse<>(false, "Trello integration not found for this organization.", null);
            }

            if (!integrationDetails.isConnected() || integrationDetails.getRefreshToken() == null) {
                log.warn("[TrelloService] Trello integration is disconnected or token missing for organization ID: {}", organizationId);
                return new SharkdomApiResponse<>(false, "Trello integration is disconnected. Please reconnect Trello.", null);
            }

            String token = integrationDetails.getRefreshToken();
            String url = String.format("%s/boards/?name=%s&key=%s&token=%s",
                    trelloBaseUrl, boardName, trelloApiKey, token);

            log.debug("[TrelloService] Trello Create Board URL: {}", url.replace(token, "****")); // mask token

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            long startTime = System.currentTimeMillis();

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            long duration = System.currentTimeMillis() - startTime;

            log.info("[TrelloService] Trello board created successfully in {} ms", duration);
            log.debug("[TrelloService] Raw Board Creation Response: {}", response.getBody());

            JsonNode boardData = objectMapper.readTree(response.getBody());
            return new SharkdomApiResponse<>(true, "Trello board created successfully", boardData);

        } catch (Exception e) {
            log.error("[TrelloService] Error while creating Trello board: {}", e.getMessage(), e);
            return new SharkdomApiResponse<>(false, "Failed to create Trello board: " + e.getMessage(), null);
        }
    }

    public SharkdomApiResponse<Object> updateCardList(String cardId, String targetListId) {
        Long organizationId = Util.getOrgIdFromToken();
        log.info("[TrelloService] Request to update Trello cardId={} to listId={} under orgId={}",
                cardId, targetListId, organizationId);

        try {
            IntegrationDetails integrationDetails =
                    integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.TRELLO);

            if (integrationDetails == null) {
                log.warn("[TrelloService] Trello integration not found for organization ID: {}", organizationId);
                return new SharkdomApiResponse<>(false, "Trello integration not found for this organization.", null);
            }

            if (!integrationDetails.isConnected() || integrationDetails.getRefreshToken() == null) {
                log.warn("[TrelloService] Trello integration is disconnected or token missing for organization ID: {}", organizationId);
                return new SharkdomApiResponse<>(false, "Trello integration is disconnected. Please reconnect Trello.", null);
            }

            String token = integrationDetails.getRefreshToken();

            String url = String.format(
                    "%s/cards/%s?key=%s&token=%s",
                    trelloBaseUrl, cardId, trelloApiKey, token
            );

            log.debug("[TrelloService] Trello Update Card URL: {}", url.replace(token, "****")); // mask token

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String requestBody = String.format("idList=%s", targetListId);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            long startTime = System.currentTimeMillis();

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[TrelloService] Trello card updated successfully in {} ms", duration);
            log.debug("[TrelloService] Raw Update Card Response: {}", response.getBody());

            JsonNode responseData = objectMapper.readTree(response.getBody());

            return new SharkdomApiResponse<>(true, "Trello card updated successfully", responseData);

        } catch (Exception e) {
            log.error("[TrelloService] Error while updating Trello card: {}", e.getMessage(), e);
            return new SharkdomApiResponse<>(false, "Failed to update Trello card: " + e.getMessage(), null);
        }
    }

    public SharkdomApiResponse<Object> getCardsByListId(String listId) {
        Long organizationId = Util.getOrgIdFromToken();
        log.info("[TrelloService] Fetching cards for listId={} under orgId={}", listId, organizationId);

        try {
            IntegrationDetails integrationDetails =
                    integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.TRELLO);

            if (integrationDetails == null) {
                log.warn("[TrelloService] Trello integration not found for organizationId={}", organizationId);
                return new SharkdomApiResponse<>(false, "Trello integration not found for this organization.", null);
            }

            if (!integrationDetails.isConnected() || integrationDetails.getRefreshToken() == null) {
                log.warn("[TrelloService] Trello integration disconnected or missing token for organizationId={}", organizationId);
                return new SharkdomApiResponse<>(false, "Trello integration is disconnected. Please reconnect Trello.", null);
            }

            String token = integrationDetails.getRefreshToken();

            String url = String.format(
                    "%s/lists/%s/cards?key=%s&token=%s",
                    trelloBaseUrl, listId, trelloApiKey, token
            );

            log.debug("[TrelloService] Trello List Cards URL: {}", url.replace(token, "****"));

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            long startTime = System.currentTimeMillis();

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            long duration = System.currentTimeMillis() - startTime;

            log.info("[TrelloService] Cards fetched successfully for listId={} in {} ms", listId, duration);
            log.debug("[TrelloService] Raw List Cards Response: {}", response.getBody());

            JsonNode cardsData = objectMapper.readTree(response.getBody());

            return new SharkdomApiResponse<>(true, "Cards fetched successfully", cardsData);

        } catch (Exception e) {
            log.error("[TrelloService] Error fetching cards for listId={}: {}", listId, e.getMessage(), e);
            return new SharkdomApiResponse<>(false, "Failed to fetch cards: " + e.getMessage(), null);
        }
    }

    public SharkdomApiResponse<List<Map<String, Object>>> getListsByBoardId(String boardId, String token) {
        long startTime = System.currentTimeMillis();

        // Build URL same as your CURL
        String url = String.format(
                "%s/boards/%s/lists?key=%s&token=%s",
                trelloBaseUrl, boardId, trelloApiKey, token
        );

        log.info("[TrelloService] Fetching Trello Lists for boardId={}", boardId);
        log.debug("[TrelloService] URL: {}", url.replace(token, "****"));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            // Optional cookie (not required but keeping as per your curl)
            headers.add("Cookie", "dsc=b7a85ed5efd6f3b8083906d109e610a4ad22e5fd69829b24b502e74a0b994963");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            long duration = System.currentTimeMillis() - startTime;
            log.info("[TrelloService] Lists fetched in {} ms with status {}", duration, response.getStatusCodeValue());

            JsonNode root = objectMapper.readTree(response.getBody());
            List<Map<String, Object>> lists = objectMapper.convertValue(root, new TypeReference<>() {});

            if (lists.isEmpty()) {
                log.warn("[TrelloService] No lists found for board {}", boardId);
                return new SharkdomApiResponse<>(true, "No lists found for this board.", lists);
            }

            for (Map<String, Object> list : lists) {
                log.info("[TrelloService] List Found: id='{}', name='{}'",
                        list.get("id"), list.get("name"));
            }

            return new SharkdomApiResponse<>(true, "Trello lists fetched successfully.", lists);

        } catch (Exception e) {
            log.error("[TrelloService] Failed to fetch lists for boardId={}: {}", boardId, e.getMessage(), e);
            return new SharkdomApiResponse<>(false, "Failed to fetch Trello lists: " + e.getMessage(), null);
        }
    }


    public SharkdomApiResponse<List<Map<String, Object>>> getListsByBoardId(String boardId) {
        Long organizationId = Util.getOrgIdFromToken();
        log.info("[TrelloService] Fetching lists for boardId={} under orgId={}", boardId, organizationId);
        IntegrationDetails integrationDetails =
                integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.TRELLO);
        if (integrationDetails == null) {
            return new SharkdomApiResponse<>(false,
                    "No Trello integration found. Please connect Trello first.", null);
        }
        if (!integrationDetails.isConnected() || integrationDetails.getRefreshToken() == null) {
            return new SharkdomApiResponse<>(false,
                    "Trello integration is disconnected. Please reconnect Trello.", null);
        }
        return getListsByBoardId(boardId, integrationDetails.getRefreshToken());
    }

    public SharkdomApiResponse<Object> createCard(
            String listId,
            String name,
            String description,
            String due,
            String start,
            String idMembers,
            String labels
    ) {

        Long organizationId = Util.getOrgIdFromToken();
        log.info("[TrelloService] Request to create Trello card in listId={} under orgId={}", listId, organizationId);

        try {
            IntegrationDetails integrationDetails =
                    integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.TRELLO);

            if (integrationDetails == null) {
                return new SharkdomApiResponse<>(false, "Trello integration not found for this organization.", null);
            }

            if (!integrationDetails.isConnected() || integrationDetails.getRefreshToken() == null) {
                return new SharkdomApiResponse<>(false, "Trello integration is disconnected. Please reconnect Trello.", null);
            }

            String token = integrationDetails.getRefreshToken();
            String url = String.format("%s/cards?key=%s&token=%s", trelloBaseUrl, trelloApiKey, token);

            log.debug("[TrelloService] Trello Create Card URL: {}", url.replace(token, "****"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("idList", listId);
            body.add("name", name);
            body.add("desc", description != null ? description : "");

            if (due != null) body.add("due", due);
            if (start != null) body.add("start", start);
            if (idMembers != null) body.add("idMembers", idMembers);
            if (labels != null) body.add("labels", labels);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            long duration = System.currentTimeMillis() - startTime;

            log.info("[TrelloService] Trello card created successfully in {} ms", duration);
            log.debug("[TrelloService] Raw Card Creation Response: {}", response.getBody());

            JsonNode cardData = objectMapper.readTree(response.getBody());

            return new SharkdomApiResponse<>(true, "Trello card created successfully", cardData);

        } catch (Exception e) {
            log.error("[TrelloService] Error while creating Trello card: {}", e.getMessage(), e);
            return new SharkdomApiResponse<>(false, "Failed to create Trello card: " + e.getMessage(), null);
        }
    }

}

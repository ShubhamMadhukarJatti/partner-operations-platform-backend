package com.sharkdom.gtm.controller;

import com.sharkdom.gtm.service.trello.TrelloBoardService;
import com.sharkdom.util.SharkdomApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/gtm")
public class TrelloRestController {

    @Autowired
    private TrelloBoardService trelloBoardService;

    @GetMapping("/trello/boards")
    public ResponseEntity<SharkdomApiResponse<List<Map<String, Object>>>> getBoardsForOrganization() {
        log.info("[TrelloController] Received request to fetch Trello boards for current organization.");
        SharkdomApiResponse<List<Map<String, Object>>> response = trelloBoardService.getUserBoards();
        if (!response.isSuccess()) {
            log.warn("[TrelloController] Trello board fetch failed: {}", response.getMessage());
            return ResponseEntity.status(400).body(response);
        }
        log.info("[TrelloController] Successfully fetched {} boards.",
                response.getData() != null ? response.getData().size() : 0);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/trello/boards/{boardId}")
    public ResponseEntity<SharkdomApiResponse<Map<String, Object>>> getBoardByIdForOrganization(
            @PathVariable("boardId") String boardId) {
        log.info("[TrelloController] Received request to fetch Trello board details for boardId={} using org token", boardId);
        SharkdomApiResponse<Map<String, Object>> response = trelloBoardService.getBoardById(boardId);
        if (!response.isSuccess()) {
            log.warn("[TrelloController] Trello board fetch failed for boardId={} (org-based): {}", boardId, response.getMessage());
            return ResponseEntity.status(400).body(response);
        }
        log.info("[TrelloController] Successfully fetched Trello board details for boardId={} (org-based)", boardId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trello/card/{cardId}")
    public SharkdomApiResponse<Object> getTrelloCard(@PathVariable String cardId) {
        log.info("[TrelloController] Received request to fetch Trello card with ID: {}", cardId);
        try {
            SharkdomApiResponse<Object> response = trelloBoardService.getCardDetails(cardId);
            if (response.isSuccess()) {
                log.info("[TrelloController] Successfully fetched Trello card details for cardId={}", cardId);
            } else {
                log.warn("[TrelloController] Failed to fetch Trello card details for cardId={} | Reason: {}",
                        cardId, response.getMessage());
            }
            return response;
        } catch (Exception e) {
            log.error("[TrelloController] Unexpected error while fetching cardId={} | {}", cardId, e.getMessage(), e);
            return new SharkdomApiResponse<>(false, "Unexpected error while fetching Trello card details", null);
        }
    }

    @PostMapping("/trello/card")
    public ResponseEntity<SharkdomApiResponse<Object>> createTrelloCard(
            @RequestParam String listId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String due,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String idMembers,
            @RequestParam(required = false) String labels
    ) {
        log.info("[TrelloController] Received request to create card with listId={}, name={}", listId, name);

        SharkdomApiResponse<Object> response = trelloBoardService.createCard(
                listId, name, description, due, start, idMembers, labels
        );

        if (response.isSuccess()) {
            log.info("[TrelloController] Trello card created successfully: {}", name);
            return ResponseEntity.ok(response);
        } else {
            log.warn("[TrelloController] Failed to create Trello card: {}", response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    @PostMapping("/trello/board")
    public ResponseEntity<SharkdomApiResponse<Object>> createTrelloBoard(
            @RequestParam String name) {
        log.info("[TrelloController] Received request to create Trello board with name={}", name);
        SharkdomApiResponse<Object> response = trelloBoardService.createBoard(name);
        if (response.isSuccess()) {
            log.info("[TrelloController] Trello board '{}' created successfully", name);
            return ResponseEntity.ok(response);
        } else {
            log.warn("[TrelloController] Failed to create Trello board '{}': {}", name, response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/trello/card/{cardId}/move")
    public ResponseEntity<SharkdomApiResponse<Object>> moveTrelloCard(
            @PathVariable String cardId,
            @RequestParam String listId) {
        log.info("[TrelloController] Request received to move card {} to list {}", cardId, listId);
        SharkdomApiResponse<Object> response = trelloBoardService.updateCardList(cardId, listId);
        if (response.isSuccess()) {
            log.info("[TrelloController] Successfully moved card {} to list {}", cardId, listId);
            return ResponseEntity.ok(response);
        } else {
            log.warn("[TrelloController] Failed to move card {} | Reason: {}", cardId, response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/trello/list/{listId}/cards")
    public ResponseEntity<SharkdomApiResponse<Object>> getCardsByList(
            @PathVariable String listId) {
        log.info("[TrelloController] Request received to fetch cards for listId={}", listId);
        SharkdomApiResponse<Object> response = trelloBoardService.getCardsByListId(listId);
        if (response.isSuccess()) {
            log.info("[TrelloController] Successfully fetched cards for listId={}", listId);
            return ResponseEntity.ok(response);
        } else {
            log.warn("[TrelloController] Failed to fetch cards for listId={} | Reason={}", listId, response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/trello/boards/{boardId}/lists")
    public ResponseEntity<SharkdomApiResponse<List<Map<String, Object>>>> getListsByBoardId(
            @PathVariable String boardId) {
        log.info("[TrelloController] Received request to fetch Trello lists for boardId={} using org token", boardId);
        SharkdomApiResponse<List<Map<String, Object>>> response =
                trelloBoardService.getListsByBoardId(boardId); // org-based token
        if (!response.isSuccess()) {
            log.warn("[TrelloController] Failed to fetch lists for boardId={} | Reason={}",
                    boardId, response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        log.info("[TrelloController] Successfully fetched {} lists for boardId={}",
                response.getData() != null ? response.getData().size() : 0,
                boardId);
        return ResponseEntity.ok(response);
    }

}

package com.sharkdom.controller.trello;

import com.sharkdom.entity.trello.TrelloBoard;
import com.sharkdom.entity.trello.TrelloList;
import com.sharkdom.entity.trello.TrelloMember;
import com.sharkdom.model.trello.TrelloCardDto;
import com.sharkdom.service.trello.TrelloService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sharkdom-trello/v1")
@RequiredArgsConstructor
public class TrelloController {

    private final TrelloService trelloService;

    @PutMapping("/boards")
    public ResponseEntity<List<TrelloBoard>> getAllBoard(@RequestParam(value = "memberId", required = false) String memberId){
        return ResponseEntity.accepted().body(trelloService.getAllBoard(memberId));
    }

    @PutMapping("/member")
    public ResponseEntity<TrelloMember> getMember(@RequestParam(value = "memberId", required = false) String memberId){
        return ResponseEntity.accepted().body(trelloService.getMember(memberId));
    }

    @PutMapping("/boards/{boardId}/lists")
    public ResponseEntity<List<TrelloList>> getListForBoard(@PathVariable(value = "boardId", required = false) String boardId){
        return ResponseEntity.accepted().body(trelloService.getListForBoard(boardId));
    }

    @PutMapping("/lists/{listId}/cards")
    public ResponseEntity<List<TrelloCardDto>> getCardForList(@PathVariable(value = "listId", required = false) String listId){
        return ResponseEntity.accepted().body(trelloService.getCardForList(listId));
    }

    @PostMapping("/cards")
    public ResponseEntity<TrelloCardDto> createCardForList(
            @RequestParam(value = "idList") String idList,
            @RequestParam(value = "name") String name,
            @RequestParam(value = "description") String description){
        return new ResponseEntity<>(trelloService.createCardForList(idList, name, description), HttpStatus.CREATED);
    }

}

package com.sharkdom.service.trello;

import com.sharkdom.entity.trello.TrelloBoard;
import com.sharkdom.entity.trello.TrelloList;
import com.sharkdom.entity.trello.TrelloMember;
import com.sharkdom.model.trello.TrelloCardDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TrelloService {

    List<TrelloBoard> getAllBoard(String memberId);

    TrelloMember getMember(String memberId);

    List<TrelloList> getListForBoard(String boardId);

    List<TrelloCardDto> getCardForList(String listId);

    TrelloCardDto createCardForList(String idList, String name, String description);
}

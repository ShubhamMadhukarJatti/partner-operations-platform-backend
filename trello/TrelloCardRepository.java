package com.sharkdom.repository.trello;

import com.sharkdom.entity.trello.TrelloCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrelloCardRepository extends JpaRepository<TrelloCard, String> {
    List<TrelloCard> findByListListId(String listId);
    List<TrelloCard> findByBoardBoardId(String boardId);
}

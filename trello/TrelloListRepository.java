package com.sharkdom.repository.trello;

import com.sharkdom.entity.trello.TrelloList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrelloListRepository extends JpaRepository<TrelloList, String> {
    List<TrelloList> findByBoardBoardId(String boardId);
}

package com.sharkdom.repository.trello;

import com.sharkdom.entity.trello.TrelloBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrelloBoardRepository extends JpaRepository<TrelloBoard, String> {
}

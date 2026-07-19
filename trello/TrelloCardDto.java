package com.sharkdom.model.trello;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrelloCardDto {

    private String cardId;

    private String name;

    private String description;
    private String url;
    private LocalDateTime dueDate;
    private boolean isClosed;
    private Integer position;

    private LocalDateTime lastActivity;

    private LocalDateTime lastSync;

    private LocalDateTime createdAt;

    private String listId;

    private String boardId;

    private Set<String> members;


}

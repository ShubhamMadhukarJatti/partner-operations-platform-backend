package com.sharkdom.model.trello.client;

import lombok.Builder;

@Builder
public record TrelloListResponse(
        String id,
        String name,
        boolean closed,
        String idBoard,
        Integer pos,
        boolean subscribed,
        Datasource datasource) {
}

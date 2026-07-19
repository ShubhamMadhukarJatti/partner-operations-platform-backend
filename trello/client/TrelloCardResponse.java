package com.sharkdom.model.trello.client;

import lombok.Builder;

import java.util.List;

@Builder
public record TrelloCardResponse(
        String id,
        String name,
        String desc,
        String url,
        String due,
        boolean closed,
        Integer pos,
        String idList,
        String idBoard,
        String dateLastActivity,
        List<String> idMembers
) {
}

package com.sharkdom.model.trello.client;

import lombok.Builder;

import java.util.List;

@Builder
public record TrelloMemberResponse(
        String id,
        String username,
        String fullName,
        String email,
        String url,
        List<String> idBoards) {
}

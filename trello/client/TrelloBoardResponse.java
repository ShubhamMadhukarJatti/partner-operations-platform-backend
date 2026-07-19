package com.sharkdom.model.trello.client;

import lombok.Builder;

import java.util.List;

@Builder
public record TrelloBoardResponse(
        String id,
        String name,
        String desc,
        String url,
        boolean closed,
        boolean pinned,
        List<Membership> memberships) {
}

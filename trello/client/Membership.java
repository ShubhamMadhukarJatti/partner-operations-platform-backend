package com.sharkdom.model.trello.client;

import lombok.Builder;

@Builder
public record Membership(
        String id,
        String idMember
) {
}

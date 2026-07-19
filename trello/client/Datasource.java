package com.sharkdom.model.trello.client;

import lombok.Builder;

@Builder
public record Datasource(
        boolean filter
) {
}

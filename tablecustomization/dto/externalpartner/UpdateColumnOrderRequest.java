package com.sharkdom.tablecustomization.dto.externalpartner;

import lombok.Data;

@Data
public class UpdateColumnOrderRequest {
    private Long columnId;
    private Integer newOrder;
}

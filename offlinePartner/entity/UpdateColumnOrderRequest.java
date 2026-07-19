package com.sharkdom.offlinePartner.entity;

import lombok.Data;
import java.util.List;

@Data
public class UpdateColumnOrderRequest {

    private List<ColumnOrder> columns;

    @Data
    public static class ColumnOrder {
        private Long columnId;
        private Integer displayOrder;
    }
}

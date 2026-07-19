package com.sharkdom.tablecustomization.dto.externalpartner;

import com.sharkdom.offlinePartner.entity.ColumnType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ColumnResponse {
    private Long columnId;
    private String name;
    private ColumnType type;
    private Integer displayOrder;
    private Boolean visible;
}


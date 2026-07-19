package com.sharkdom.tablecustomization.dto.externalpartner;

import com.sharkdom.offlinePartner.entity.ColumnType;
import lombok.Data;

@Data
public class ExternalPartnerColumnRequestDTO {
    private Long tableId;
    private String name;
    private ColumnType type;
    private Integer displayOrder;
    private Boolean visible = true;
}


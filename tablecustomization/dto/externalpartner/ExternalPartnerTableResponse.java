package com.sharkdom.tablecustomization.dto.externalpartner;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ExternalPartnerTableResponse {
    private Long tableId;
    private String tableName;
    private Long orgId;
    private List<ColumnResponse> columns;
    private List<RowResponse> rows;
}

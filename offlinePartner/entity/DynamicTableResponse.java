package com.sharkdom.offlinePartner.entity;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DynamicTableResponse {

    private Long tableId;
    private String tableName;
    private List<ColumnDto> columns;
    private List<RowDto> rows;
}
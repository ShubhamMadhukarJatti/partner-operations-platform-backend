package com.sharkdom.tablecustomization.dto.externalpartner;

import lombok.Data;

import java.util.Map;

@Data
public class UpdateRowValuesRequest {
    private Long rowId;

    // columnId -> value
    private Map<Long, String> values;
}


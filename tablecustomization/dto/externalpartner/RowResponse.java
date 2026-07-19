package com.sharkdom.tablecustomization.dto.externalpartner;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RowResponse {
    private Long rowId;
    private Map<Long, String> values;
}


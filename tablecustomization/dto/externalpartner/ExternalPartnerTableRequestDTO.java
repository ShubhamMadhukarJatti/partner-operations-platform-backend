package com.sharkdom.tablecustomization.dto.externalpartner;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExternalPartnerTableRequestDTO {
    private Long orgId;
    private String tableName;
}


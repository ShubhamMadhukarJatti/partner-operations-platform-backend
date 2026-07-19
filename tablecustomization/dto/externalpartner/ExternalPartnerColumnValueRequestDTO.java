package com.sharkdom.tablecustomization.dto.externalpartner;

import lombok.Data;

@Data
public class ExternalPartnerColumnValueRequestDTO {
    private Long rowId;
    private Long columnId;
    private String value;
}


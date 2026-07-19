package com.sharkdom.tablecustomization.dto.externalpartner;

import lombok.Data;

@Data
public class CreateColumnExternalPartnerRequest {
    private Long tableId;
    private String name;
    private String type; // TEXT, NUMBER, TAG, STATUS, DATE
}


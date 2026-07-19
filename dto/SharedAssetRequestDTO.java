package com.sharkdom.partnerattribution.dto;

import lombok.Data;

@Data
public class SharedAssetRequestDTO {

    private Long partnerOrgId;
    private String dealId;
    private String title;
    private String fileUrl;
    private String username;
}
package com.sharkdom.partnerattribution.dto;

import lombok.Data;

@Data
public class SharedAssetResponseDTO {

    private Long id;
    private String title;
    private String fileUrl;
    private String sharedBy;
    private String dealId;
}
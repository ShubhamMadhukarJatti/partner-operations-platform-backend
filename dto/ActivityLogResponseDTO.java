package com.sharkdom.partnerattribution.dto;

import lombok.Data;

@Data
public class ActivityLogResponseDTO {

    private String title;
    private String description;
    private String date;
    private String actor;
    private String type;
    private String dealId;
}

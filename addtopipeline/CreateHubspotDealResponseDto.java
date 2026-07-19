package com.sharkdom.partnerattribution.addtopipeline;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateHubspotDealResponseDto {

    private String hubspotDealId;

    private String response;

    private String message;
}
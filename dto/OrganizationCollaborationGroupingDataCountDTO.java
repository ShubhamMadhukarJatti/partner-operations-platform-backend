package com.sharkdom.dto;

import lombok.Data;

@Data
public class OrganizationCollaborationGroupingDataCountDTO {

    private Long totalPartnersCount;
    private Long reliablePartnersCount;
    private Long steadyPartnersCount;
    private Long lowImpactPartnersCount;
    private Long inactivePartnersCount;

}

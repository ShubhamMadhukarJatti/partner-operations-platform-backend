package com.sharkdom.deals.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class DealDetailsResponse {
    private Date creationTimestamp;
    private String dealId;
    private Long organizationId;
    private String offerDetail;
    private String[] restrictedSectors;
    private String[] channelAllowed;
    private String quotaRemaining;
    private String geography;
    private boolean approvalRequired;
    private String status;
    private String organizationName;
    private String logoUrl;
    private String organizationType;
    private String organizationBrief;
}

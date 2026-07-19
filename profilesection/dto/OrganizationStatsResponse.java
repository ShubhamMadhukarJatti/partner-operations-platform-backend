package com.sharkdom.profilesection.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrganizationStatsResponse {

    private int views;
    private int inquiries;
    private int rank;
    private Double visibilityScore;
    private boolean isEliteBadgeApplicable;
}
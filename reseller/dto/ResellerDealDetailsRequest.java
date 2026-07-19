package com.sharkdom.reseller.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ResellerDealDetailsRequest {
    private Long vendorOrgId;
    private Long expectedReleaseTime;
    private Date expectedReleaseDate;
    private String resellerMode;
    private String productPlanRequired;
    private Long numberOfLicences;
    private String calculatedPartnerTier;
    private String billingModel;
    private Double actualPrice;
    private Double buyPrice;
}

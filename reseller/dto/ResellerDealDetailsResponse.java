package com.sharkdom.reseller.dto;

import com.sharkdom.reseller.entity.ResellerDealSource;
import com.sharkdom.reseller.entity.ResellerDealStag;
import com.sharkdom.reseller.entity.ResellerDealStatus;
import lombok.Data;

import java.util.Date;

@Data
public class ResellerDealDetailsResponse {

    private Long id;
    private Long resellerOrgId;
    private Long vendorOrgId;
    private String partnerName;
    private Long expectedReleaseTime;
    private Date expectedReleaseDate;
    private String resellerMode;
    private String productPlanRequired;
    private Long numberOfLicences;
    private String calculatedPartnerTier;
    private String billingModel;
    private Double actualPrice;
    private Double buyPrice;
    private String poc;
    private ResellerDealStatus resellerDealStatus;
    private ResellerDealStag resellerDealStag;
    private ResellerDealSource resellerDealSource;
}

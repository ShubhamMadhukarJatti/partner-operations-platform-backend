package com.sharkdom.model.partnerDeals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.constants.partnerDeals.DealSource;
import com.sharkdom.constants.partnerDeals.DealStage;
import com.sharkdom.constants.partnerDeals.DealStatus;
import com.sharkdom.entity.partenearDeals.Deal;
import lombok.Data;

import java.util.Map;

@Data
public class DealRequestDto {

    private String customerAccountName;
    private String website;
    private String headQuarterLocation;
    private Integer estimatedAcv;
    private Integer expectedClosingTime;
    private String currentSolution;
    private String requirements;
    private Map<String, CustomFieldDto> customFields;
    private DealStage dealStage;
    private DealSource source;
    private Boolean isApproved;
    private Long dealerOrgId;
    private Long vendorOrgId;
    private Long dealProtectionPeriod;
    private DealStatus dealStatus;
    private String dealSize;
    private String userId;
    private boolean isExternalPartnerPortalDeal;
    private boolean isInternalToExternalPartnerPortalDeal;
    private String externalPartnerCode;


    public Deal toEntity() {
        Deal deal = new Deal();
        deal.setCustomerAccountName(this.customerAccountName);
        deal.setWebsite(this.website);
        deal.setHeadQuarterLocation(this.headQuarterLocation);
        deal.setEstimatedAcv(this.estimatedAcv);
        deal.setExpectedClosingTime(this.expectedClosingTime);
        deal.setCurrentSolution(this.currentSolution);
        deal.setRequirements(this.requirements);
        deal.setDealStage(this.dealStage);
        deal.setSource(this.source);
        deal.setIsApproved(this.isApproved);
        deal.setDealerOrgId(this.dealerOrgId);
        deal.setVendorOrgId(this.vendorOrgId);
        deal.setDealProtectionPeriod(this.dealProtectionPeriod);
        deal.setDealSize(this.dealSize);
        deal.setDealStatus(this.dealStatus);
        deal.setIsExternalPartnerPortalDeal(this.isExternalPartnerPortalDeal);

        if (customFields != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(customFields);
                deal.setCustomFields(json);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Invalid customFields format", e);
            }
        }

        return deal;
    }
}

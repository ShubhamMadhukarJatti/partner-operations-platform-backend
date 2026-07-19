package com.sharkdom.model.referral;

import com.sharkdom.entity.referral.CampaignEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
public class CampaignResponse {
    private List<CampaignEntity> campaignDetails;
    private Double leadsChange;
    private Double partnerChange;
    private Long leadsCount;
    private Long partnerCount;
}

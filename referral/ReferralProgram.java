package com.sharkdom.model.referral;

import com.sharkdom.constants.campaign.CampaignStatus;
import lombok.Data;

@Data
public class ReferralProgram {
    private String programName;
    private CampaignStatus status;
    private String joinedDate;
    private Double rating;
    private String topComment;
    private ReferralMetrics metrics;
}

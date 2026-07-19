package com.sharkdom.model.referral;

import lombok.Data;

import java.util.List;

@Data
public class PartnerDetails {
    private String name;
    private String description;
    private String status;
    private String memberSince;
    private PartnerInformation partnerInformation;
    private PerformanceOverview performanceOverview;
    private List<ReferralProgram> referralPrograms;
}

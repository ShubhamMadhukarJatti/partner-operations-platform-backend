package com.sharkdom.partnerprogram.dtos;

import com.sharkdom.partnerprogram.enums.*;
import lombok.Data;

@Data
public class CompanyPartnerApplicationDTO {

    private Long id;
    private String companyName;
    private String companyWebsite;
    private String primaryContactName;
    private String contactEmail;

    private CompanySize companySize;
    private PartnerType partnerType;
    private String icpFitExplanation;
    private PartnerProgramMaturity maturity;

    private Boolean hasExistingRelationship;
    private Boolean informationConfirmed;
    private Boolean agreedToTerms;

    private ApplicationStatus status;
    private String reviewComments;
}
package com.sharkdom.partnerattribution.dto;
import lombok.Data;

@Data
public class PartnerIntroEmailRequestDTO {

    private String partnerContactName;
    private String partnerContactTitle;
    private String partnerCompany;

    private String targetContactName;
    private String targetContactTitle;
    private String targetAccountName;

    private String senderName;
    private String senderTitle;
    private String senderCompany;

    private String partnerRelationshipType;
    private String partnerRelationshipDuration;

    private String whyMakingIntro;
    private String senderCompanyDescription;
    private String relevanceToTarget;

    private String relationshipTone;

}
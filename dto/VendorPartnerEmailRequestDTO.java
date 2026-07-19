package com.sharkdom.partnerattribution.dto;


import lombok.Data;

@Data
public class VendorPartnerEmailRequestDTO {

    private String senderName;
    private String senderTitle;
    private String senderCompany;

    private String partnerContactName;
    private String partnerContactTitle;
    private String partnerCompany;

    private String targetContactName;
    private String targetContactTitle;
    private String targetAccountName;

    private String yourDealStage;
    private String partnerRelationshipType;
    private String partnerRelationshipDuration;
    private String relevanceReason;
    private String priorContext;

}
package com.sharkdom.partnerattribution.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.partnerattribution.enums.OpportunityType;
import com.sharkdom.partnerattribution.enums.SenderType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartnerIntroductionDTO {

    @JsonProperty("sender_type")
    private SenderType senderType;

    @JsonProperty("opportunity_type")
    private OpportunityType opportunityType;

    @JsonProperty("sender_name")
    private String senderName;

    @JsonProperty("sender_title")
    private String senderTitle;

    @JsonProperty("sender_company")
    private String senderCompany;

    @JsonProperty("sender_company_description")
    private String senderCompanyDescription;

    @JsonProperty("partner_contact_name")
    private String partnerContactName;

    @JsonProperty("partner_contact_title")
    private String partnerContactTitle;

    @JsonProperty("partner_company")
    private String partnerCompany;

    @JsonProperty("target_contact_name")
    private String targetContactName;

    @JsonProperty("target_contact_title")
    private String targetContactTitle;

    @JsonProperty("target_account_name")
    private String targetAccountName;

    @JsonProperty("partner_relationship_type")
    private String partnerRelationshipType;

    @JsonProperty("partner_relationship_duration")
    private String partnerRelationshipDuration;

    @JsonProperty("relationship_tone")
    private String relationshipTone;

    @JsonProperty("why_making_intro")
    private String whyMakingIntro;

    @JsonProperty("relevance_reason")
    private String relevanceReason;

    @JsonProperty("relevance_to_target")
    private String relevanceToTarget;

    @JsonProperty("prior_context")
    private String priorContext;

    @JsonProperty("your_deal_stage")
    private String yourDealStage;
}
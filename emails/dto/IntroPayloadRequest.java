package com.sharkdom.partnerattribution.emails.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class IntroPayloadRequest {

    private String senderName;
    private String senderTitle;
    private String senderCompany;
    private String senderCompanyDescription;
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
    private String whyMeetingPreferred;
    private String meetingDuration;
    private String meetingAgenda;
    private String availableSlots;
    private String whySuggesting;
    private String endorsementStrength;
    private String targetContactLinkedin;
    private String linkedinIntroFormat;
    private String partnerContactLinkedin;
    private String linkedinConnectionType;
    private String relevanceToTarget;
    private String relationshipTone;
    private String whyMakingIntro;
}
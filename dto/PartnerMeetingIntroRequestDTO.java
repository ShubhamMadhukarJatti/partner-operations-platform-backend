package com.sharkdom.partnerattribution.dto;

import lombok.Data;
import java.util.List;

@Data
public class PartnerMeetingIntroRequestDTO {

    private String partnerContactName;
    private String partnerContactTitle;
    private String partnerCompany;

    private String targetContactName;
    private String targetContactTitle;
    private String targetAccountName;

    private String senderName;
    private String senderCompany;

    private String partnerRelationshipType;

    private String senderCompanyDescription;

    private String whySuggesting;

    private String meetingAgenda;

    private String meetingDuration;

    private List<String> availableSlots;

    private String endorsementStrength;
}
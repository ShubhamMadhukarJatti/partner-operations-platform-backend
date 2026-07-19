package com.sharkdom.partnerattribution.dto;

import lombok.Data;

@Data
public class VendorMeetingIntroRequestDTO {

    private String senderName;
    private String senderTitle;
    private String senderCompany;

    private String partnerContactName;
    private String partnerContactTitle;
    private String partnerCompany;

    private String targetContactName;
    private String targetAccountName;

    private String partnerRelationshipType;
    private String partnerRelationshipDuration;

    private String whyMeetingPreferred;

    private String meetingDuration;
    private String meetingAgenda;

    private String availableSlots;

}
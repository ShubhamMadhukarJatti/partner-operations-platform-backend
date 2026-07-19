package com.sharkdom.model.email;

import com.sharkdom.constants.stripe.StripePlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateOrganizationEmailReqModel {

    String templateCode;
    List<Long> organizationIds;
    String s3AttachmentNames;
    String docUrl;
    String confirmUrl;
    String meetingLink;
    String calendarLink;
    String emailVerifyLink;
    String subscriptionName;
    Long subscriptionPrice;
    LocalDate subscriptionRenewal;
    String subscriptionBenefits;
    String partnerOrganizationName;
    LocalDate partnershipInitiationTime;
    LocalDate partnershipExpirationTime;
    LocalDate partnershipAcceptTime;
    String organization1Name;
    String organization2Name;
    String organization1Desc;
    String organization2Desc;
    Integer organizationCount;
    String organization1Logo;
    String organization2Logo;
    String followingDate;
    String followerOrganizationName;
    String followingOrganizationName;
    private String emailUnsubscribeLink;
    private String senderOrganizationName;
    private String organizationName;
    private String meetingTime;
    private String organizationCode;
    private String message;
}

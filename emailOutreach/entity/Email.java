package com.sharkdom.emailOutreach.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "emails")
public class Email extends BaseEntity {

    @JsonProperty("from")
    @Column(name="FROM_ADDRESS", length = 255)
    private String from;

    @JsonProperty("to")
    @Column(name="TO_ADDRESS", length = 255)
    private String to;

    @JsonProperty("subject")
    @Column(name="SUBJECT", length = 255)
    private String subject;

    @JsonProperty("body")
    @Column(name="BODY", columnDefinition = "TEXT")
    private String body;

    @JsonProperty("message_id")
    @Column(name="MESSAGE_ID", columnDefinition = "TEXT")
    private String messageId;

    @JsonProperty("org_id")
    @Column(name="ORG_ID", length = 50)
    private Long orgId;

    @JsonProperty("partner_org_id")
    @Column(name="PARTNER_ORG_ID", length = 50)
    private Long partnerOrgId;

    @JsonProperty("thread_id")
    @Column(name="THREAD_ID", length = 255)
    private String threadId;

    @JsonProperty("is_external_partner")
    @Column(name = "IS_EXTERNAL_PARTNER", columnDefinition = "BOOLEAN DEFAULT false")
    private boolean isExternalPartner = false;

    @JsonProperty("external_partner_code")
    @Column(name="EXTERNAL_PARTNER_CODE")
    private String externalPartnerCode;

    @JsonProperty("sender_org_email")
    @Column(name="SENDER_ORG_EMAIL", columnDefinition = "TEXT")
    private String senderOrgEmail = "";

}

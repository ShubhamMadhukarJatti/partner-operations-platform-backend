package com.sharkdom.zoho.entity;

import com.sharkdom.constants.MouStatus;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "zoho_signed_document")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZohoSignedDocumentEntity extends BaseEntity {
    private String senderDocumentId;
    private String recipientDocumentId;
    private String senderRequestId;
    private String recipientRequestId;
    private String senderActionId;
    private String recipientActionId;
    private Date senderSignedAt;
    private Date recipientSignedAt;
    private String senderEmail;
    private String recipientEmail;
    private String senderIp;
    private String recipientIp;
    private MouStatus status;
    private Long offlinePartnerId;
    private String offlinePartnerCode;
    private Long organizationCollaborationId;
}

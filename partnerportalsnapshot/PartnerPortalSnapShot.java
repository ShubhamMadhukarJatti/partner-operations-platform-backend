package com.sharkdom.entity.partnerportalsnapshot;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.enums.partnerportalsnapshot.PartnerPortalSnapshotAccess;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name="t_partner_portal_snapshot")
public class PartnerPortalSnapShot extends BaseEntity {

    @Column(name="SENDER_ORG_ID")
    private Long senderOrganizationId;

    @Column(name="external_partner_code")
    private String externalPartnerCode;

    @Column(name="RECEIVER_USER_EMAIL")
    private String receiverUserEmail;

    @Column(name="RECEIVER_USER_ID")
    private String receiverUserId;

    @Enumerated(EnumType.STRING)
    @Column(name="ACCESS_LEVEL")
    private PartnerPortalSnapshotAccess partnerPortalSnapshotAccess;

    @Column(columnDefinition = "boolean default false")
    private boolean isProgressChartShared=false;

    @Column(columnDefinition = "boolean default false")
    private boolean isNotesAndAttachmentsShared=false;

    @Column(name="shared_url",columnDefinition = "TEXT")
    private String shared_url;
}

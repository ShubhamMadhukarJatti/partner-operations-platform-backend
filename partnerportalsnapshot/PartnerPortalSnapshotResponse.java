package com.sharkdom.entity.partnerportalsnapshot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartnerPortalSnapshotResponse {

    private Long id;
    private Long senderOrganizationId;
    private String senderOrganizationName;
    private String receiverUserEmail;
    private String receiverUserId;
    private String accessLevel;
    private boolean progressChartShared;
    private boolean notesAndAttachmentsShared;
    private String sharedUrl;
}
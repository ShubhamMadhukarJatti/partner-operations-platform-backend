package com.sharkdom.offlinePartner.model;

import lombok.Data;

@Data
public class OfflinePartnerDocumentRequest {
    Long organizationId;
    String email;
    String effectiveDate;
    String expiringDate;
    String count;
    String docId;
}

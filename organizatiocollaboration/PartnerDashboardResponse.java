package com.sharkdom.model.organizatiocollaboration;

import java.util.Date;

public record PartnerDashboardResponse(
        Long organizationCollaborationId,
        String organizationName,
        String logoUrl,
        CollaborationCategory collaborationCategory,
        Date creationTimestamp, String status,
        Long partnerOrganizationId) {
}

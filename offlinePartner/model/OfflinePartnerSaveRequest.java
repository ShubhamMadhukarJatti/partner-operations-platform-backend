package com.sharkdom.offlinePartner.model;

import java.util.List;

public record OfflinePartnerSaveRequest(Long organizationId, List<OfflinePartnerSaveDetail> partnerInviteDetails) {
}

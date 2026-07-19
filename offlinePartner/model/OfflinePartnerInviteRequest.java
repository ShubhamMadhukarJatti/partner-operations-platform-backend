package com.sharkdom.offlinePartner.model;

import java.util.List;

public record OfflinePartnerInviteRequest(Long organizationId, List<String> emails, boolean sendAll) {
}

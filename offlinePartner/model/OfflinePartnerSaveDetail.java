package com.sharkdom.offlinePartner.model;

public record OfflinePartnerSaveDetail(String partnerName,
                                       String remarks,
                                       String email,
                                       boolean isMember) {
}

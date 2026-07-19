package com.sharkdom.offlinePartner.model;

import java.util.List;

public record GroupPartnerRequest(Long organizationId, List<String> emails, PartnerGroup partnerGroup) {
}

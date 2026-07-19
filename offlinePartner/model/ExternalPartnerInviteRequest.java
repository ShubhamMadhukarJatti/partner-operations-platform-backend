package com.sharkdom.offlinePartner.model;

public record ExternalPartnerInviteRequest(
        Long organizationId,
        String email,
        String name
) {}
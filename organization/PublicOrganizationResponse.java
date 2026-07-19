package com.sharkdom.model.organization;

import com.sharkdom.entity.organization.PreferredPartnershipTypes;
import com.sharkdom.entity.organization.PreferredSector;

import java.util.List;

public record PublicOrganizationResponse(String name, String briefDescription, Double rating,
                                         List<PreferredPartnershipTypes> preferredPartnershipTypes, Long id,
                                         List<PreferredSector> preferredSectors, Long acknowledgmentTime,
                                         String logoUrl) {
}


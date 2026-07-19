package com.sharkdom.dto;

import com.sharkdom.entity.organization.PreferredPartnershipTypes;
import com.sharkdom.entity.organization.PreferredSector;
import com.sharkdom.entity.organization.RegionToPartnerWith;
import com.sharkdom.entity.organization.TeamSize;
import lombok.Data;

import java.util.List;

@Data
public class PartnershipDetailsResponse {

    private String registrationType;

    private TeamSize partnershipTeamSize;

    private List<String> goalsToUseSharkdom;

    private List<PreferredPartnershipTypes> preferredPartnershipTypes;

    private List<RegionToPartnerWith> regionToPartnerWith;

    private String targetMarket;

    private String companyType;

    private String onboardedPartners;

    private List<PreferredSector> preferredSectors;
}
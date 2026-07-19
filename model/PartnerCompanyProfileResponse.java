package com.sharkdom.agenticai.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PartnerCompanyProfileResponse {

    private Long id;
    private String companyName;
    private String avgPartnerSourceRevenue;
    private Long partnerShipTeamSize;
    private String subsectors;
    private String description;
    private String about;
    private String website;
    private String partnerRange;
    private String compliances;
    private List<PartnerShipTeamResponse> partnerShipTeam;
}
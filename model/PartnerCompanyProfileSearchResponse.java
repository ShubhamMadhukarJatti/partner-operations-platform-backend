package com.sharkdom.agenticai.model;

import com.sharkdom.agenticai.entity.PartnerShipTeam;
import lombok.Data;

import java.util.List;

@Data
public class PartnerCompanyProfileSearchResponse {

    private Long id;
    private String companyName;
    private String subsectors;
    private String compliances;
    private String description;
    private String website;
    private List<PartnerShipTeam> partnerShipTeam;
    private String partnerRange;
    private String about;
    private String avgPartnerSourceRevenue;
    private Long partnerShipTeamSize;

}

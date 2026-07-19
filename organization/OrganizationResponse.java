package com.sharkdom.model.organization;

import com.sharkdom.entity.organization.OrganizationServices;
import com.sharkdom.entity.organization.PreferredPartnershipTypes;
import com.sharkdom.entity.organization.PreferredSector;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationResponse {
    private long id;
    private String code;
    private String name;
    private String about;
    private String briefDescription;
    private String sector;
    private String stage;
    private String city;
    private String state;
    private boolean verified;
    private int inceptionYear;
    private String targetMarket;
    private Double rating;
    private String logoUrl;
    private List<PreferredSector> preferredSectors;
    private List<PreferredPartnershipTypes> preferredPartnershipTypes;
    private List<OrganizationServices> services;
    private String companyType;
    private Long acknowledgmentTime;
    private Long activePartnerships;
    private Long pipelinePartnerships;
    private String sectorType;


}

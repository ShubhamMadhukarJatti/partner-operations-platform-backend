package com.sharkdom.profilesection.dto;

import com.sharkdom.entity.organization.ServingCustomersType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrganizationProfileSectionResponse {

    private String companyName;
    private String tagline;
    private String headquarter;
    private String about;
    private String foundedIn;
    private String industries;
    private List<ServingCustomersType> servedCustomers;
    private String coverImageURL;
}
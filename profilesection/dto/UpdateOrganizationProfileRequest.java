package com.sharkdom.profilesection.dto;

import com.sharkdom.entity.organization.ServingCustomersType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UpdateOrganizationProfileRequest {

    private String companyName;

    private String tagline;

    private String headquarter;

    private String about;

    private String foundedIn;

    private String industries;

    private List<ServingCustomersType> servedCustomers;
}

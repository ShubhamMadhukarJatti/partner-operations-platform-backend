package com.sharkdom.agenticai.model;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class PartnerCompanyProfileRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 255)
    private String companyName;

    @NotBlank(message = "Average partner sourced revenue is required")
    private String avgPartnerSourceRevenue;

    @NotNull(message = "Partnership team size is required")
    @Min(0)
    private Long partnerShipTeamSize;

    @Valid
    private List<PartnerShipTeamRequest> partnerShipTeam;

    private String subsectors;

    private String compliances;

    @NotBlank(message = "Description is required")
    private String description;

    private String about;

    @Pattern(regexp = "^(http|https)://.*$", message = "Website must be valid URL")
    private String website;

    private String partnerRange;
}
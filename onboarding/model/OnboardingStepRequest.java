package com.sharkdom.onboarding.model;

import com.sharkdom.entity.organization.RegionToPartnerWith;
import com.sharkdom.entity.organization.TeamSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OnboardingStepRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Company URL is required")
    private String companyURL;

    @NotBlank(message = "Team participation is required")
    private String teamParticipation;

    @NotBlank(message = "Market segment is required")
    private String marketSegment;

    @NotNull(message = "GTM team size is required")
    private TeamSize gtmTeamSize;

    @NotBlank(message = "Current partners is required")
    private String currentPartners;

    @NotEmpty(message = "At least one goal is required")
    private List<String> goalsWithSharkdom;

    @NotEmpty(message = "At least one preferred region is required")
    private List<RegionToPartnerWith> preferredRegion;

    @NotEmpty(message = "At least one preferred partnership is required")
    private List<String> preferredPartnerships;

    @NotBlank(message = "Email is required")
    private String email;
}



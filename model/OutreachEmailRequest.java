package com.sharkdom.agenticai.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OutreachEmailRequest {

    @NotNull
    @JsonProperty("ORGid")
    private Long ORGid;

    @NotBlank
    @JsonProperty("ORGname")
    private String ORGname;

    private String about;

    @JsonProperty("partnership_lead_name")
    private String partnershipLeadName;

    @JsonProperty("partnership_lead_title")
    private String partnershipLeadTitle;

    @JsonProperty("compliance_stack")
    private List<String> complianceStack;

    @JsonProperty("partner_program")
    private Boolean partnerProgram;

    @JsonProperty("team_size")
    private String teamSize;

    @JsonProperty("revenue_range")
    private String revenueRange;

    private String subsectors;

    @JsonProperty("match_score")
    private Integer matchScore;

    private String channel;
}
package com.sharkdom.agenticai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnterpriseLinkedinMessageRequest {

    @JsonProperty("ORGid")
    private Long orgId;

    @JsonProperty("ORGname")
    private String orgName;

    private String about;

    @JsonProperty("partnership_lead_name")
    private String partnershipLeadName;

    @JsonProperty("partnership_lead_title")
    private String partnershipLeadTitle;

    @JsonProperty("partnership_lead_linkedin")
    private String partnershipLeadLinkedin;

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
package com.sharkdom.agenticai.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EnterpriseLinkedinInsufficientSignalRequest {

    @JsonProperty("ORGid")
    private Long orgId;

    @JsonProperty("ORGname")
    private String orgName;

    @JsonProperty("partnership_lead_name")
    private String partnershipLeadName;

    @JsonProperty("partnership_lead_title")
    private String partnershipLeadTitle;

    @JsonProperty("compliance_stack")
    private List<String> complianceStack;

    @JsonProperty("partner_program")
    private Boolean partnerProgram;

    @JsonProperty("match_score")
    private Integer matchScore;

    private String channel;
}
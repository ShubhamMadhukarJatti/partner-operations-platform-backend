package com.sharkdom.agenticai.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OutreachGenerateRequest {

    @NotNull
    private Long ORGid;

    @NotBlank
    private String ORGname;

    private String about;

    private String partnership_lead_name;

    private String partnership_lead_title;

    private String partnership_lead_linkedin;

    private List<String> compliance_stack;

    private Boolean partner_program;

    private String partner_program_url;

    private String team_size;

    private String revenue_range;

    private String subsectors;

    private Integer match_score;

    private String channel;

}
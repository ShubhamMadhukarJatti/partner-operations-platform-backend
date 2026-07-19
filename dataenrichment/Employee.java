package com.sharkdom.dataenrichment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Employee {

    private String name;

    private String headline;

    @JsonProperty("current_role")
    private String currentRole;

    @JsonProperty("current_company")
    private String currentCompany;

    private String location;

    @JsonProperty("profile_url")
    private String profileUrl;

    @JsonProperty("photo_url")
    private String photoUrl;

    @JsonProperty("is_current_role")
    private Boolean isCurrentRole;

    @JsonProperty("role_end_date")
    private String roleEndDate;

    @JsonProperty("designation_score")
    private Integer designationScore;
}
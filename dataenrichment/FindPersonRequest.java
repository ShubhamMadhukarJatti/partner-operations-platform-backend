package com.sharkdom.dataenrichment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FindPersonRequest {

    private String name;

    private String company;

    private String location;

    private String title;

    private String education;

    private Integer limit;

    @JsonProperty("include_emails")
    private Boolean includeEmails;

    @JsonProperty("user_id")
    private String userId;
}
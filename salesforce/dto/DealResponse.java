package com.sharkdom.salesforce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DealResponse {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Amount")
    private Double amount;

    @JsonProperty("StageName")
    private String stageName;

    @JsonProperty("LastModifiedDate")
    private String lastModifiedDate;
}

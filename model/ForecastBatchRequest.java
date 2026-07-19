package com.sharkdom.agenticai.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ForecastBatchRequest {

    @JsonProperty("ORGid")
    private Long ORGid;

    @JsonProperty("ORGname")
    private String ORGname;

    private String about;
    private String partnership_type;
    private String sector;
    private String subsector;
    private Integer meetingSuccessRate;
    private Integer activePartnerships;
    private Integer average_revenue;

}
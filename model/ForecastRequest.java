package com.sharkdom.agenticai.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ForecastRequest {

    @JsonProperty("ORGid")
    private Long ORGid;

    @JsonProperty("ORGname")
    private String ORGname;

    private String about;
    private String briefDescription;
    private String website;
    private String logoUrl;

    private Integer meetingSuccessRate;
    private Integer acknowledgmentTime;

    private Integer activePartnerships;
    private Integer pipelinePartnerships;

    private String legalName;

    private List<String> preferredSectors;
    private List<String> subsectors;

    private String partnership_type;
    private String sector;
    private String subsector;

    private Integer average_revenue;
}
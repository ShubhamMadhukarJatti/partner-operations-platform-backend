package com.sharkdom.entity.organization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationCustomResponse {
    private long id;
    private String name;
    private String about;
    private String briefDescription;
    private String website;
    private String logoUrl;
    private Long meetingSuccessRate;
    private Long acknowledgmentTime;
    private Long activePartnerships;
    private Long pipelinePartnerships;
    private String legalName;
    private List<String> preferredSectors;
    private List<String> filters;
    private String email;
    private boolean isSelectedForExternalPartnerships;
    private boolean isShortlisted;
    private List<String> certifications;
    private Integer matchScore;
    private Double trustPilotScore;
    private Integer trustPilotReviewsCount;
}

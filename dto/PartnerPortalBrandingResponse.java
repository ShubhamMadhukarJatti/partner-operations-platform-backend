package com.sharkdom.dto;

import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartnerPortalBrandingResponse {

    private Long id;
    private String title;
    private String description;
    private Long organizationId;
    private String url;
    private Boolean enabledReferralProgram;
    private Date createdDate;
    private Boolean applicationReviewTimeAllotted;
    private Boolean PartnerTierAllotted;
    private Boolean discountAllotted;
    private Integer totalClicks;
    private Integer totalSubmits;
    private Integer numberOfQuestions;
    private Integer timeToFill;

}

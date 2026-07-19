package com.sharkdom.partnertraining.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartnerReadinessDto {

    private String partnerName;
    private long noOfUsers;
    private long coursesEnrolled;
    private double readinessScore;
}


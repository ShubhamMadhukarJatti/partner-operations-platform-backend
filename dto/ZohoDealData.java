package com.sharkdom.partnerattribution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZohoDealData {

    private Integer employees;

    private String industry;

    private String country;

    private String stage;
}
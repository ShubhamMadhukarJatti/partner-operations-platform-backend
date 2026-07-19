package com.sharkdom.partnerattribution.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class SalesforceDealData {

    private Integer employees;
    private String industry;
    private String country;
    private String stage;

}

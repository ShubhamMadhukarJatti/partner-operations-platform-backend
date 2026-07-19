package com.sharkdom.dto;

import lombok.Data;

import java.util.Date;

@Data
public class CompanyDetailsResponse {
    private String name;
    private Date incorporationDate;
    private String website;
    private Boolean isInHousePartnership;
    private String about;
    private String aboutProductService;
    private String productUrl;
}

package com.sharkdom.dto;

import lombok.Data;

import java.util.Date;

@Data
public class CompanyDetailsRequest {
    private String name;
    private String about;
    private Date incorporationDate;
    private Boolean isInHousePartnership;
    private String website;
    private String productUrl;
    private String aboutProductService;
}

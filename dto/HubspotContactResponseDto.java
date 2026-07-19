package com.sharkdom.partnerattribution.dto;


import lombok.Data;

@Data
public class HubspotContactResponseDto {

    private String email;
    private String fullName;
    private String jobTitle;
    private String linkedInUrl;
    private String leadStatus;
    private String phone;
    private String lastActivityDate;
    private String contactOwner;

}
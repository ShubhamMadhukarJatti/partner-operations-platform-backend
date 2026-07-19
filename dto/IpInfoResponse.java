package com.sharkdom.dto;

import lombok.Data;

@Data
public class IpInfoResponse {

    private String ip;
    private String city;
    private String region;
    private String country;
    private String loc;
    private String org;
    private String postal;
    private String timezone;
}

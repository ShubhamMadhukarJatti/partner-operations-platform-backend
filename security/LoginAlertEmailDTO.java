package com.sharkdom.security;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginAlertEmailDTO {
    private String email;
    private String ip;
    private String city;
    private String region;
    private String country;
    private String timezone;
}
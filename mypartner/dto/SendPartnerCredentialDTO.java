package com.sharkdom.mypartner.dto;

import lombok.Data;

@Data
public class SendPartnerCredentialDTO {
    private String url;
    private String username;
    private String password;
    private String partnerId;
}

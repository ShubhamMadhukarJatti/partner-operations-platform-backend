package com.sharkdom.mypartner.dto;

import lombok.Data;

@Data
public class SendPartnerTraningCredentialDTO {
    private String url;
    private String username;
    private String password;
    private Long partnerId;
}

package com.sharkdom.emailOutreach.dto;

import lombok.Data;

@Data
public class SendMailRequest {
    private String to;
    private String from;
    private String subject;
    private String body;
    private Long partnerId;
    private String externalPartnerCode;
}

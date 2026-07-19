package com.sharkdom.emailOutreach.dto;

import lombok.Data;

@Data
public class SendMailResponse {
    private String id;
    private String message;
    private String threadId;
}

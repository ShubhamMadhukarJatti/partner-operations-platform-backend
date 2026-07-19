package com.sharkdom.agenticai.model;

import lombok.Data;

@Data
public class SendConnectionRequestRequest {
    private String targetLinkedinUrl;
    private String note;
    private String accountId;
}
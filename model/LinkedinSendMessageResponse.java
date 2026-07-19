package com.sharkdom.agenticai.model;

import lombok.Data;

@Data
public class LinkedinSendMessageResponse {
    private String status;
    private String error;
    private String reason;

    private String displayStatus;
    private String displayMessage;
}

package com.sharkdom.emailagent.dto;

import lombok.Data;

@Data
public class EmailRequest {
    private Partner partner;
    private String trigger_type;
    private int max_drafts;
}
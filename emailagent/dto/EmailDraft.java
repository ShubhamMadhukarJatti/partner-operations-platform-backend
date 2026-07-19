package com.sharkdom.emailagent.dto;

import lombok.Data;

@Data
public class EmailDraft {
    private String subject;
    private String body;
    private String tone;
    private String cta;
    private Double estimated_reply_probability;
    private String rationale;
    private String first_name;
    private String company_name;
}
package com.sharkdom.agenticai.model;

import lombok.Data;

@Data
public class ChatRequest {
    private String prompt;
    private String session_id;
    private String account_id;
}
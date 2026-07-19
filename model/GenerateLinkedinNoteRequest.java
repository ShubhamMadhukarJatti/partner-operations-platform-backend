package com.sharkdom.agenticai.model;

import lombok.Data;

@Data
public class GenerateLinkedinNoteRequest {
    private String contactName;
    private String companyName;
    private String context;
}
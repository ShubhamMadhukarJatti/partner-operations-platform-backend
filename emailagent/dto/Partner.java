package com.sharkdom.emailagent.dto;

import lombok.Data;

@Data
public class Partner {
    private String partner_name;
    private String company;
    private String lifecycle_stage;
    private String engagement_level;
    private String focus_area;
    private String recent_activity;
    private Manager manager;
    private String additional_notes;
}
package com.sharkdom.partnerattribution.dto;

import lombok.Data;

@Data
public class ActionDTO {

    private String type;
    private String label;
    private String redirectUrl;
    private String api;
    private String method;

}
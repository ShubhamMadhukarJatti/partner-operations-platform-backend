package com.sharkdom.agenticai.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewHealthResponse {

    private String status;
    private String service;
    private String version;
    private String timestamp;

    private String displayStatus;

}
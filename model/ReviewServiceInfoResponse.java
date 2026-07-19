package com.sharkdom.agenticai.model;


import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ReviewServiceInfoResponse {

    private String service;
    private String version;
    private String description;
    private Map<String, String> endpoints;

    private String displayStatus;

}
package com.sharkdom.agenticai.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ReviewFetchResponse {

    private Boolean success;

    private ReviewFetchData data;

    private String error;

    private Map<String, Object> details;

    private String warning;

    private String rawResponse;

    private String displayMessage;

}
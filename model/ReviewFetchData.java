package com.sharkdom.agenticai.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewFetchData {

    private String orgName;
    private String requestedOrg;
    private String requestedUrl;

    private TrustpilotReview trustpilot;

    private String fetchedAt;

}
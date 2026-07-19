package com.sharkdom.agenticai.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrustpilotReview {

    private Boolean found;
    private Double rating;
    private String label;
    private Integer totalReviews;
    private Object starDistribution;
    private String summary;
    private String profileUrl;

}

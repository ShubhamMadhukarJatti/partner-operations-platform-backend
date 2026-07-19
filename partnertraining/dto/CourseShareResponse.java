package com.sharkdom.partnertraining.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseShareResponse {

    private Long id;
    private Long courseId;
    private String receiverUserEmail;
    private String sharedUrl;
    private boolean active;
}
package com.sharkdom.partnertraining.dto;

import lombok.Data;

@Data
public class CourseShareRequest {
    private Long courseId;
    private String receiverUserEmail;
}
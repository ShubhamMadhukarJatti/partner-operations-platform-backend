package com.sharkdom.partnertraining.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseCertificateResponseDTO {

    private Long courseId;
    private String courseName;
    private String certificateUrl;
}
package com.sharkdom.partnertraining.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CourseDashboardResponse {

    private List<CourseCardDto> continueCourses;
    private List<CourseCardDto> courses;

    private long totalElements;
    private int totalPages;
    private int currentPage;
}


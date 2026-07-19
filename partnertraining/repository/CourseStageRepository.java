package com.sharkdom.partnertraining.repository;

import com.sharkdom.partnertraining.entity.CourseStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseStageRepository extends JpaRepository<CourseStage,Long> {
    // Only count active stages
    int countByCourseIdAndActiveTrue(Long courseId);

    // Only fetch active stages
    List<CourseStage> findByCourseIdAndActiveTrue(Long courseId);

    List<CourseStage> findByCourseIdAndActiveTrueOrderByStageOrderAsc(Long courseId);
}

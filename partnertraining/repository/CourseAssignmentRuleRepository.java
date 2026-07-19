package com.sharkdom.partnertraining.repository;

import com.sharkdom.partnertraining.entity.CourseAssignmentRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseAssignmentRuleRepository extends JpaRepository<CourseAssignmentRule, Long> {

    Optional<CourseAssignmentRule> findByCourseId(Long courseId);

    boolean existsByCourseId(Long courseId);
}


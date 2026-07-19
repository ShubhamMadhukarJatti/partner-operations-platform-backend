package com.sharkdom.partnertraining.repository;

import com.sharkdom.partnertraining.entity.UserCourseStageProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCourseStageProgressRepository
        extends JpaRepository<UserCourseStageProgress, Long> {

    Optional<UserCourseStageProgress>
    findByUserIdAndCourseIdAndStageId(
            String userId, Long courseId, Long stageId);

    long countByUserIdAndCourseIdAndCompletedTrue(
            String userId, Long courseId);

    List<UserCourseStageProgress>
    findByUserIdAndCourseId(String userId, Long courseId);

    Optional<UserCourseStageProgress>
    findByOrgIdAndCourseIdAndStageId(
            Long orgId, Long courseId, Long stageId);

    long countByOrgIdAndCourseIdAndCompletedTrue(
            Long orgId, Long courseId);

    List<UserCourseStageProgress>
    findByOrgIdAndCourseId(Long orgId, Long courseId);
}


package com.sharkdom.partnertraining.repository;

import com.sharkdom.partnertraining.entity.UserCourseStatusEntity;
import com.sharkdom.partnertraining.enums.UserCourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCourseStatusRepository
        extends JpaRepository<UserCourseStatusEntity, Long> {

    Optional<UserCourseStatusEntity> findByUserIdAndCourseId(
            String userId, Long courseId
    );

    List<UserCourseStatusEntity> findAllByUserIdAndStatus(
            String userId,
            UserCourseStatus status
    );

    Page<UserCourseStatusEntity> findByUserId(
            String userId,
            Pageable pageable
    );

    Page<UserCourseStatusEntity> findByUserIdAndStatus(
            String userId,
            UserCourseStatus status,
            Pageable pageable
    );

    List<UserCourseStatusEntity> findAllByUserId(String userId);

    long countByUserIdAndStatus(
            String userId,
            UserCourseStatus status
    );

    long countByCourseId(Long courseId);

    long countByCourseIdAndStatus(Long courseId, UserCourseStatus status);

    List<UserCourseStatusEntity> findAllByAssigningOrgId(Long assigningOrgId);

}

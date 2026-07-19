package com.sharkdom.partnertraining.repository;

import com.sharkdom.partnertraining.entity.MyPartnerUserCourseStatusEntity;
import com.sharkdom.partnertraining.enums.UserCourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MyPartnerUserCourseStatusRepository
        extends JpaRepository<MyPartnerUserCourseStatusEntity, Long> {

    Optional<MyPartnerUserCourseStatusEntity>
    findByCourseIdAndAssignedOrgId(
            Long courseId,
            Long assignedOrgId
    );

    Optional<MyPartnerUserCourseStatusEntity>
    findByCourseIdAndAssigningOrgId(
            Long courseId,
            Long assignedOrgId
    );

    List<MyPartnerUserCourseStatusEntity>
    findAllByAssignedOrgIdAndStatus(
            Long assignedOrgId,
            UserCourseStatus status
    );

    Page<MyPartnerUserCourseStatusEntity>
    findByAssignedOrgId(
            Long assignedOrgId,
            Pageable pageable
    );

    Page<MyPartnerUserCourseStatusEntity>
    findByAssignedOrgIdAndStatus(
            Long assignedOrgId,
            UserCourseStatus status,
            Pageable pageable
    );

    long countByAssigningOrgId(Long assigningOrgId);

    long countByAssignedOrgId(Long assigningOrgId);

    long countByAssignedOrgIdAndStatus(
            Long assigningOrgId,
            UserCourseStatus status
    );

    long countByAssigningOrgIdAndStatus(
            Long assigningOrgId,
            UserCourseStatus status
    );

    long countByAssigningOrgIdAndCourseId(
            Long assigningOrgId,
            Long courseId
    );

    long countByAssigningOrgIdAndCourseIdAndStatus(
            Long assigningOrgId,
            Long courseId,
            UserCourseStatus status
    );

    List<MyPartnerUserCourseStatusEntity>
    findAllByAssigningOrgId(Long assigningOrgId);

    List<MyPartnerUserCourseStatusEntity> findAllByCourseId(Long courseId);

    long countDistinctAssignedOrgIdByCourseId(Long courseId);

    long countByCourseIdAndStatus(Long courseId, UserCourseStatus status);

}
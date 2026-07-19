package com.sharkdom.partnertraining.repository;

import com.sharkdom.partnertraining.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course,Long> {

//    Page<Course> findAll(Pageable pageable);
//
//    long countByCreatedById(Long organizationId);
//
//    List<Course> findAllByCreatedBy_Id(Long organizationId);

    Page<Course> findAllByPublishedTrue(Pageable pageable);

    long countByCreatedBy_IdAndPublishedTrue(Long organizationId);

    List<Course> findAllByCreatedBy_IdAndPublishedTrue(Long organizationId);

    Page<Course> findAllByPublishedFalse(Pageable pageable);

    List<Course> findAllByCreatedBy_IdAndPublishedFalse(Long organizationId);

}

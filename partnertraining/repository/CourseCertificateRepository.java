package com.sharkdom.partnertraining.repository;

import com.sharkdom.partnertraining.entity.CourseCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseCertificateRepository
        extends JpaRepository<CourseCertificate, Long> {

    List<CourseCertificate> findByUserId(String userId);

    List<CourseCertificate> findByOrgId(Long orgId);
}
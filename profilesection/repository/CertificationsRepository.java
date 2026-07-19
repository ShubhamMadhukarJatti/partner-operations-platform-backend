package com.sharkdom.profilesection.repository;

import com.sharkdom.profilesection.entity.OrganizationCertificationsConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificationsRepository
        extends JpaRepository<OrganizationCertificationsConfig, Long> {
}
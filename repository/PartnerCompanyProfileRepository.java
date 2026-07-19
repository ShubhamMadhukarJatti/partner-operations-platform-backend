package com.sharkdom.agenticai.repository;

import com.sharkdom.agenticai.entity.PartnerCompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartnerCompanyProfileRepository
        extends JpaRepository<PartnerCompanyProfile, Long>,
        JpaSpecificationExecutor<PartnerCompanyProfile> {

    // Find by company name
    Optional<PartnerCompanyProfile> findByCompanyName(String companyName);

    // If you want search capability later
    boolean existsByCompanyName(String companyName);

    boolean existsByCompanyNameIgnoreCase(String companyName);
}
package com.sharkdom.profilesection.repository;

import com.sharkdom.profilesection.entity.OrganizationPartnerProgram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationPartnerProgramRepository extends JpaRepository<OrganizationPartnerProgram, Long> {

    Optional<OrganizationPartnerProgram> findByOrganizationId(Long organizationId);

}
package com.sharkdom.profilesection.repository;

import com.sharkdom.profilesection.entity.OrganizationSearchCounter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationSearchCounterRepository extends JpaRepository<OrganizationSearchCounter, Long> {

    Optional<OrganizationSearchCounter> findByOrganizationId(Long organizationId);
}

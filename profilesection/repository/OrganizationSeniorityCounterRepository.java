package com.sharkdom.profilesection.repository;

import com.sharkdom.profilesection.entity.OrganizationSeniorityCounter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationSeniorityCounterRepository
        extends JpaRepository<OrganizationSeniorityCounter, Long> {

    Optional<OrganizationSeniorityCounter> findByOrganizationId(Long organizationId);
}
package com.sharkdom.repository.organization;

import com.sharkdom.entity.organization.OrganizationAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationAvailabilityRepository extends JpaRepository<OrganizationAvailability, Long> {
    Optional<OrganizationAvailability> findByOrganizationId(Long organizationId);
}

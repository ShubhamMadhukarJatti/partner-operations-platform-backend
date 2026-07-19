package com.sharkdom.repository.organization;

import com.sharkdom.entity.organization.GettingStartedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GettingStartedRepository extends JpaRepository<GettingStartedEntity, Long> {
    Optional<GettingStartedEntity> findByOrganizationId(Long organizationId);
}

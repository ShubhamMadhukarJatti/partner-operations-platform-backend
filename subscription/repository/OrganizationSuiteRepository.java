package com.sharkdom.subscription.repository;

import com.sharkdom.subscription.entity.OrganizationSuite;
import com.sharkdom.subscription.entity.SuiteKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationSuiteRepository
        extends JpaRepository<OrganizationSuite, Long> {

    boolean existsByOrganizationIdAndSuiteKeyAndActiveTrue(Long orgId, SuiteKey suiteKey);

    List<OrganizationSuite> findByOrganizationIdAndActiveTrue(Long orgId);
}

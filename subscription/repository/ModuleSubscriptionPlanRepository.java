package com.sharkdom.subscription.repository;

import com.sharkdom.subscription.entity.ModuleSubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuleSubscriptionPlanRepository
        extends JpaRepository<ModuleSubscriptionPlan, Long> {

    Optional<ModuleSubscriptionPlan> findByOrgId(Long orgId);
}

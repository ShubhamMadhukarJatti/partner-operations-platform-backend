package com.sharkdom.subscription.repository;

import com.sharkdom.subscription.entity.ModuleSubscriptionPlan;
import com.sharkdom.subscription.entity.UserSubscriptionPlanFreeTrail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuleSubscriptionPlanFreeTrailRepository
        extends JpaRepository<UserSubscriptionPlanFreeTrail, Long> {

    Optional<UserSubscriptionPlanFreeTrail> findByUserEmail(String email);
}

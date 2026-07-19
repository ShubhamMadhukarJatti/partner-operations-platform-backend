package com.sharkdom.repository.stripe;

import com.sharkdom.constants.stripe.StripePlanType;
import com.sharkdom.entity.stripe.StripePlanConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StripePlanConfigurationRepository extends JpaRepository<StripePlanConfiguration, StripePlanType> {

    @Query("SELECT u.priceId FROM StripePlanConfiguration u WHERE u.planType = :stripePlanType")
    String findPriceIdByPlanType(@Param("stripePlanType") StripePlanType stripePlanType);

    @Query("SELECT u.planType FROM StripePlanConfiguration u WHERE u.priceId = :priceId")
    Optional<StripePlanType> findPlanTypeByPriceId(@Param("priceId") String priceId);
}

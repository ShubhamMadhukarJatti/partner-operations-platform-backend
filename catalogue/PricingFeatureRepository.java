package com.sharkdom.repository.catalogue;

import com.sharkdom.entity.catalogue.PricingFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PricingFeatureRepository
        extends JpaRepository<PricingFeature, Long> {

    Optional<PricingFeature> findByFeatureName(String featureName);
}

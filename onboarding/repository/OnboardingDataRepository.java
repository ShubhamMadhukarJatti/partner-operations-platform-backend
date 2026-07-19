package com.sharkdom.onboarding.repository;

import com.sharkdom.onboarding.entity.OnboardingData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OnboardingDataRepository extends JpaRepository<OnboardingData,Long> {

    Optional<OnboardingData> findByCompanyURLIgnoreCase(String companyURL);

    Optional<OnboardingData> findByEmailIgnoreCase(String email);
}

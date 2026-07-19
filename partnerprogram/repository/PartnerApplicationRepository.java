package com.sharkdom.partnerprogram.repository;

import com.sharkdom.partnerprogram.entities.PartnerApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartnerApplicationRepository extends JpaRepository<PartnerApplication, Long> {

    boolean existsByEmail(String email);

    Optional<PartnerApplication> findByEmail(String email);

    Optional<PartnerApplication> findByUserId(String userId);
}
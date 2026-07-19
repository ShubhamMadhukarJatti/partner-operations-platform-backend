package com.sharkdom.reseller.repository;

import com.sharkdom.reseller.entity.ResellerDealLicense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResellerDealLicenseRepository extends JpaRepository<ResellerDealLicense,Long> {
    Optional<ResellerDealLicense> findByCustomerId(Long customerId);
}

package com.sharkdom.reseller.repository;

import com.sharkdom.reseller.entity.ResellerDealCustomer;
import com.sharkdom.reseller.entity.ResellerDealLicense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResellerDealCustomerRepository extends JpaRepository<ResellerDealCustomer,Long> {

    Page<ResellerDealCustomer> findByResellerDealId(Long resellerDealId, Pageable pageable);

}

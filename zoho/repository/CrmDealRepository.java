package com.sharkdom.zoho.repository;

import com.sharkdom.zoho.entity.CrmDeal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrmDealRepository
        extends JpaRepository<CrmDeal, Long> {

    Optional<CrmDeal>
    findByZohoRecordId(String zohoRecordId);
}
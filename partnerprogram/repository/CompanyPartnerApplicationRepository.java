package com.sharkdom.partnerprogram.repository;


import com.sharkdom.partnerprogram.entities.CompanyPartnerApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyPartnerApplicationRepository
        extends JpaRepository<CompanyPartnerApplication, Long> {
}
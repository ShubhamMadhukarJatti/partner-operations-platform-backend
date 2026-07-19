package com.sharkdom.partnerprogram.repository;

import com.sharkdom.partnerprogram.entities.ConsultantPartnerApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultantPartnerApplicationRepository
        extends JpaRepository<ConsultantPartnerApplication, Long> {
}
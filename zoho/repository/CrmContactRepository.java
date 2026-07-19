package com.sharkdom.zoho.repository;

import com.sharkdom.zoho.entity.CrmContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrmContactRepository
        extends JpaRepository<CrmContact, Long> {

    Optional<CrmContact>
    findByZohoRecordId(String zohoRecordId);
}

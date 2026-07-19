package com.sharkdom.zoho.repository;

import com.sharkdom.zoho.entity.ZohoCrmAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrmAccountRepository
        extends JpaRepository<ZohoCrmAccount, Long> {

    Optional<ZohoCrmAccount>
    findByZohoRecordId(String zohoRecordId);
}

package com.sharkdom.zoho.repository;

import com.sharkdom.zoho.entity.ZohoConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ZohoConnectionRepository
        extends JpaRepository<ZohoConnection, Long> {

    Optional<ZohoConnection> findByTenantId(Long tenantId);
}
package com.sharkdom.tablecustomization.repository.externalpartner;

import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExternalPartnerTableRepository extends JpaRepository<ExternalPartnerTable,Long> {
    boolean existsByOrgIdAndTableName(Long orgId, String tableName);
    Optional<ExternalPartnerTable> findByOrgId(Long orgId);
}

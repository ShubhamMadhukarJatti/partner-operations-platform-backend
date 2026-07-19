package com.sharkdom.tablecustomization.repository.overlaprecordfieldentity;

import com.sharkdom.tablecustomization.entity.overlaprecordfieldentity.OverlapRecordFieldEntityTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OverlapRecordFieldEntityTableRepository extends JpaRepository<OverlapRecordFieldEntityTable, Long> {
    Optional<OverlapRecordFieldEntityTable> findByOrgId(Long orgId);
}

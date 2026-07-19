package com.sharkdom.tablecustomization.repository.overlaprecordfieldentity;

import com.sharkdom.tablecustomization.entity.overlaprecordfieldentity.OverlapRecordFieldEntityRow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OverlapRecordFieldEntityTableRowRepository extends JpaRepository<OverlapRecordFieldEntityRow,Long> {

    Optional<OverlapRecordFieldEntityRow>
    findByTableIdAndSourceId(Long tableId, String sourceId);
}

package com.sharkdom.tablecustomization.repository.overlaprecordfieldentity;

import com.sharkdom.tablecustomization.entity.overlaprecordfieldentity.OverlapRecordFieldEntityTable;
import com.sharkdom.tablecustomization.entity.overlaprecordfieldentity.OverlapRecordFieldEntityTableColumn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OverlapRecordFieldEntityTableColumnRepository extends JpaRepository<OverlapRecordFieldEntityTableColumn,Long> {

    List<OverlapRecordFieldEntityTableColumn>
    findByTableAndVisibleTrueOrderByDisplayOrderAsc(
            OverlapRecordFieldEntityTable table
    );

    boolean existsByTableAndNameIgnoreCaseAndVisibleTrue(
            OverlapRecordFieldEntityTable table,
            String name
    );

    List<OverlapRecordFieldEntityTableColumn>
    findByTableIdAndVisibleTrue(Long tableId);

    List<OverlapRecordFieldEntityTableColumn>
    findByTableIdAndVisibleTrueOrderByDisplayOrderAsc(Long tableId);

}

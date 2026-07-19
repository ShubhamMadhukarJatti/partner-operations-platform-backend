package com.sharkdom.tablecustomization.repository.overlaprecordfieldentity;

import com.sharkdom.tablecustomization.entity.overlaprecordfieldentity.OverlapRecordFieldEntityColumnValue;
import com.sharkdom.tablecustomization.entity.overlaprecordfieldentity.OverlapRecordFieldEntityTableColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OverlapRecordFieldEntityTableColumnValueRepository extends JpaRepository<OverlapRecordFieldEntityColumnValue,Long> {
    boolean existsByRowIdAndColumnId(Long rowId, Long columnId);

    List<OverlapRecordFieldEntityColumnValue> findByRowIdAndColumnIdIn(
            Long rowId,
            List<Long> columnIds
    );

    @Query("""
SELECT v.row.id
FROM OverlapRecordFieldEntityColumnValue v
WHERE v.column.id = :columnId
AND v.value = :value
""")
    List<Long> findRowIdsByColumnIdAndValue(
            Long columnId,
            String value
    );

    List<OverlapRecordFieldEntityColumnValue>
    findByRowIdIn(List<Long> rowIds);

    Optional<OverlapRecordFieldEntityColumnValue> findByRowIdAndColumnId(
            Long rowId,
            Long columnId
    );

}

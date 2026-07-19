package com.sharkdom.offlinePartner.repository;

import com.sharkdom.offlinePartner.entity.ColumnValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ColumnValueRepository extends JpaRepository<ColumnValue, Long> {

    List<ColumnValue> findByRowId(Long rowId);

    Optional<ColumnValue> findByRowIdAndColumnId(Long rowId, Long columnId);

    @Query("""
    select cv from ColumnValue cv
    where cv.row.table.id = :tableId """)
    List<ColumnValue> findByTableId(Long tableId);

    void deleteByColumnId(Long columnId);

}
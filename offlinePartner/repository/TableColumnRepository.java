package com.sharkdom.offlinePartner.repository;

import com.sharkdom.offlinePartner.entity.TableColumn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TableColumnRepository extends JpaRepository<TableColumn,Long> {

    List<TableColumn> findByTableIdOrderByDisplayOrderAsc(Long tableId);

    List<TableColumn> findByTableId(Long tableId);

    boolean existsByTableIdAndName(Long tableId, String name);

    Optional<TableColumn> findByIdAndTableId(Long id, Long tableId);

    List<TableColumn> findByTableIdIn(List<Long> tableIds);
}

package com.sharkdom.offlinePartner.repository;

import com.sharkdom.offlinePartner.entity.TableRow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TableRowRepository extends JpaRepository<TableRow, Long> {

    List<TableRow> findByTableId(Long tableId);

    Optional<TableRow> findByIdAndTableId(Long id, Long tableId);

}
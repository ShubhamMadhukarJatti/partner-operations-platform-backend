package com.sharkdom.tablecustomization.repository.externalpartner;

import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerColumnValue;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTableColumn;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTableRow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExternalPartnerTableColumnValueRepository extends JpaRepository<ExternalPartnerColumnValue,Long> {

    Optional<ExternalPartnerColumnValue> findByRow_IdAndColumn_Id(
            Long rowId, Long columnId
    );

    boolean existsByRowAndColumn(ExternalPartnerTableRow row, ExternalPartnerTableColumn column);

    Optional<ExternalPartnerColumnValue> findByRowAndColumn(
            ExternalPartnerTableRow row,
            ExternalPartnerTableColumn column
    );

    List<ExternalPartnerColumnValue> findByRow(
            ExternalPartnerTableRow row
    );

    List<ExternalPartnerColumnValue> findByRowIn(
            List<ExternalPartnerTableRow> rows
    );



}

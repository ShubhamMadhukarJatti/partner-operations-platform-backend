package com.sharkdom.tablecustomization.repository.externalpartner;

import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTable;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTableRow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExternalPartnerTableRowRepository extends JpaRepository<ExternalPartnerTableRow,Long> {
    Optional<ExternalPartnerTableRow> findByTableAndSourceId(
            ExternalPartnerTable table,
            String sourceId
    );

    List<ExternalPartnerTableRow> findByTableOrderByCreationTimestampAsc(
            ExternalPartnerTable table
    );

    List<ExternalPartnerTableRow> findByTableOrderByIdAsc(
            ExternalPartnerTable table
    );



}

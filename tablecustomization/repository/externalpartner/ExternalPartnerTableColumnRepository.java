package com.sharkdom.tablecustomization.repository.externalpartner;

import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTable;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTableColumn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExternalPartnerTableColumnRepository extends JpaRepository<ExternalPartnerTableColumn,Long> {
    boolean existsByTable_IdAndName(Long tableId, String name);

    List<ExternalPartnerTableColumn> findByTableOrderByDisplayOrderAsc(ExternalPartnerTable table);

    boolean existsByTableAndNameIgnoreCaseAndVisibleTrue(
            ExternalPartnerTable table, String name
    );

    List<ExternalPartnerTableColumn> findByTableAndVisibleTrueOrderByDisplayOrderAsc(
            ExternalPartnerTable table
    );

}

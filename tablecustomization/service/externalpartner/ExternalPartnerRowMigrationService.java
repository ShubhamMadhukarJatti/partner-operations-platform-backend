package com.sharkdom.tablecustomization.service.externalpartner;

import com.sharkdom.offlinePartner.entity.OfflinePartnerInvite;
import com.sharkdom.offlinePartner.repository.OfflinePartnerInviteRepository;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerColumnValue;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTable;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTableColumn;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTableRow;
import com.sharkdom.tablecustomization.repository.externalpartner.ExternalPartnerTableColumnRepository;
import com.sharkdom.tablecustomization.repository.externalpartner.ExternalPartnerTableColumnValueRepository;
import com.sharkdom.tablecustomization.repository.externalpartner.ExternalPartnerTableRepository;
import com.sharkdom.tablecustomization.repository.externalpartner.ExternalPartnerTableRowRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExternalPartnerRowMigrationService {

    private final OfflinePartnerInviteRepository offlineRepo;
    private final ExternalPartnerTableRepository tableRepo;
    private final ExternalPartnerTableRowRepository rowRepo;
    private final ExternalPartnerTableColumnRepository columnRepo;
    private final ExternalPartnerTableColumnValueRepository valueRepo;

    public void migrateOfflinePartnersToDynamicTable() {

        List<OfflinePartnerInvite> partners = offlineRepo.findAll();

        for (OfflinePartnerInvite partner : partners) {

            if (partner.getOrganizationId() == null) continue;

            ExternalPartnerTable table = tableRepo
                    .findByOrgId(partner.getOrganizationId())
                    .orElseThrow(() ->
                            new IllegalStateException("No table found for org " + partner.getOrganizationId())
                    );

            // ======================
            // CREATE OR GET ROW
            // ======================
            String sourceId = partner.getId().toString();

            ExternalPartnerTableRow row = rowRepo
                    .findByTableAndSourceId(table, sourceId)
                    .orElseGet(() -> {
                        ExternalPartnerTableRow newRow = new ExternalPartnerTableRow();
                        newRow.setTable(table);
                        newRow.setSourceId(sourceId);

                        ExternalPartnerTableRow saved = rowRepo.saveAndFlush(newRow);

                        log.debug("Created new row with ID {}", saved.getId());
                        return saved;
                    });

            // ======================
            // CREATE COLUMN VALUES
            // ======================
            List<ExternalPartnerTableColumn> columns =
                    columnRepo.findByTableOrderByDisplayOrderAsc(table);

            for (ExternalPartnerTableColumn column : columns) {

                boolean exists = valueRepo.existsByRowAndColumn(row, column);
                if (exists) continue;

                String value = resolveValue(column.getName(), partner);

                ExternalPartnerColumnValue cell =
                        ExternalPartnerColumnValue.builder()
                                .row(row)
                                .column(column)
                                .value(value)
                                .build();

                valueRepo.save(cell);
            }

            log.info("Migrated partner {} → table {}", partner.getId(), table.getId());
        }
    }

    // ===========================
    // COLUMN → ENTITY MAPPING
    // ===========================
    private String resolveValue(String columnName, OfflinePartnerInvite p) {

        return switch (columnName) {
            case "Poc Email" -> p.getEmail();
            case "Status" -> p.getStatus();
            case "Remarks" -> p.getRemarks();
            case "Partner Group" ->
                    p.getPartnerGroup() != null ? p.getPartnerGroup().name() : null;
            case "Company Name" -> p.getPartnerName();
            case "Verify Email Sent" -> String.valueOf(p.isVerifyEmailSent());
            case "Message Code" -> p.getOfflinePartnerMessageCode();
            case "Is Member" -> String.valueOf(p.isMember());
            default -> null;
        };
    }
}

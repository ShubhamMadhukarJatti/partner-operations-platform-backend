package com.sharkdom.tablecustomization.service.externalpartner;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.offlinePartner.entity.ColumnType;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTable;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTableColumn;
import com.sharkdom.tablecustomization.repository.externalpartner.ExternalPartnerTableColumnRepository;
import com.sharkdom.tablecustomization.repository.externalpartner.ExternalPartnerTableRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalPartnerColumnMigrationService {

    private final ExternalPartnerTableRepository tableRepository;
    private final ExternalPartnerTableColumnRepository columnRepository;

    private static final List<ColumnSeed> DEFAULT_COLUMNS = List.of(
            new ColumnSeed("Poc Email", ColumnType.TEXT, 1),
            new ColumnSeed("Status", ColumnType.STATUS, 2),
            new ColumnSeed("Remarks", ColumnType.TEXT, 3),
            new ColumnSeed("Partner Group", ColumnType.TAG, 4),
            new ColumnSeed("Company Name", ColumnType.TEXT, 5),
            new ColumnSeed("Verify Email Sent", ColumnType.STATUS, 6),
            new ColumnSeed("Message Code", ColumnType.TEXT, 7),
            new ColumnSeed("Is Member", ColumnType.STATUS, 8),
            new ColumnSeed("User ID", ColumnType.TEXT, 9)
    );

    // =========================
    // MIGRATE / SEED COLUMNS
    // =========================
    @Transactional
    public void seedDefaultColumnsForAllTables() {

        log.info("EP_COLUMN_SEED_START");

        try {
            List<ExternalPartnerTable> tables = tableRepository.findAll();

            if (tables.isEmpty()) {
                log.warn("EP_COLUMN_SEED_NO_TABLES_FOUND");
                return;
            }

            int created = 0;
            int skipped = 0;

            for (ExternalPartnerTable table : tables) {

                log.info("EP_COLUMN_SEED_TABLE | tableId={} | orgId={}",
                        table.getId(), table.getOrgId());

                for (ColumnSeed seed : DEFAULT_COLUMNS) {

                    boolean exists =
                            columnRepository.existsByTable_IdAndName(
                                    table.getId(), seed.name()
                            );

                    if (exists) {
                        skipped++;
                        log.debug("EP_COLUMN_EXISTS | tableId={} | name={}",
                                table.getId(), seed.name());
                        continue;
                    }

                    ExternalPartnerTableColumn column =
                            ExternalPartnerTableColumn.builder()
                                    .name(seed.name())
                                    .type(seed.type())
                                    .displayOrder(seed.order())
                                    .visible(true)
                                    .table(table)
                                    .build();

                    columnRepository.save(column);
                    created++;

                    log.info("EP_COLUMN_CREATED | tableId={} | columnName={} | type={}",
                            table.getId(), seed.name(), seed.type());
                }
            }

            log.info("EP_COLUMN_SEED_COMPLETE | created={} | skipped={}",
                    created, skipped);

        } catch (Exception ex) {

            log.error("EP_COLUMN_SEED_FAILED | error={}", ex.getMessage(), ex);

            throw new ServiceException(
                    ErrorMessages.SH185,
                    ex.getMessage()
            );
        }
    }

    // =========================
    // INTERNAL RECORD
    // =========================
    private record ColumnSeed(String name, ColumnType type, int order) {}
}

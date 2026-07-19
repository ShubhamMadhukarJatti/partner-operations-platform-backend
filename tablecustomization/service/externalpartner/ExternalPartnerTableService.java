package com.sharkdom.tablecustomization.service.externalpartner;

import com.sharkdom.offlinePartner.entity.ColumnType;
import com.sharkdom.offlinePartner.entity.OfflinePartnerInvite;
import com.sharkdom.offlinePartner.service.DynamicTableService;
import com.sharkdom.tablecustomization.dto.OrgColumnsResponse;
import com.sharkdom.tablecustomization.dto.externalpartner.*;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerColumnValue;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTable;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTableColumn;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTableRow;
import com.sharkdom.tablecustomization.repository.externalpartner.ExternalPartnerTableColumnRepository;
import com.sharkdom.tablecustomization.repository.externalpartner.ExternalPartnerTableColumnValueRepository;
import com.sharkdom.tablecustomization.repository.externalpartner.ExternalPartnerTableRepository;
import com.sharkdom.tablecustomization.repository.externalpartner.ExternalPartnerTableRowRepository;
import com.sharkdom.util.Util;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalPartnerTableService {

    private final ExternalPartnerTableRepository tableRepo;
    private final ExternalPartnerTableColumnRepository columnRepo;
    private final ExternalPartnerTableRowRepository rowRepo;
    private final ExternalPartnerTableColumnValueRepository valueRepo;
    private final DynamicTableService dynamicTableService;

    @Transactional
    public void processOfflinePartnerRow(
            Long orgId,
            OfflinePartnerInvite invite
    ) {
        ExternalPartnerTable table =
                findOrCreateTable(orgId, "ExternalPartner Table");

        seedDefaultColumnsIfMissing(table);

        ExternalPartnerTableRow row =
                createRow(table, invite.getId());

        insertRowValues(table, row, invite);
    }


    public ExternalPartnerTable findOrCreateTable(Long orgId, String tableName) {

        return tableRepo
                .findByOrgId(orgId)
                .orElseGet(() -> {
                    ExternalPartnerTable table = ExternalPartnerTable.builder()
                            .orgId(orgId)
                            .tableName(tableName)
                            .build();

                    log.info("CREATED TABLE → orgId={}, name={}", orgId, tableName);
                    return tableRepo.save(table);
                });
    }

    private static final List<ColumnSeed> DEFAULT_COLUMNS = List.of(
            new ColumnSeed("Poc Email", ColumnType.TEXT, 1),
            new ColumnSeed("Status", ColumnType.STATUS, 2),
            new ColumnSeed("Remarks", ColumnType.TEXT, 3),
            new ColumnSeed("Partner Group", ColumnType.TAG, 4),
            new ColumnSeed("Company Name", ColumnType.TEXT, 5),
            new ColumnSeed("Verify Email Sent", ColumnType.STATUS, 6),
            new ColumnSeed("Message Code", ColumnType.TEXT, 7),
            new ColumnSeed("Is Member", ColumnType.STATUS, 8),
            new ColumnSeed("Poc", ColumnType.TEXT, 9)
    );

    @Transactional
    public void seedDefaultColumnsIfMissing(ExternalPartnerTable table) {

        Map<String, ExternalPartnerTableColumn> existingColumns =
                columnRepo.findByTableAndVisibleTrueOrderByDisplayOrderAsc(table)
                        .stream()
                        .collect(Collectors.toMap(
                                c -> c.getName().toLowerCase(),
                                Function.identity()
                        ));

        for (ColumnSeed seed : DEFAULT_COLUMNS) {
            if (!existingColumns.containsKey(seed.name().toLowerCase())) {

                ExternalPartnerTableColumn column =
                        ExternalPartnerTableColumn.builder()
                                .table(table)
                                .name(seed.name())
                                .type(seed.type())
                                .displayOrder(seed.order())
                                .visible(true)
                                .build();

                columnRepo.save(column);

                log.info("COLUMN CREATED → tableId={}, name={}",
                        table.getId(), seed.name());
            }
        }
    }


    @Transactional
    public ExternalPartnerTableRow createRow(
            ExternalPartnerTable table,
            Long sourceId
    ) {
        ExternalPartnerTableRow row =
                ExternalPartnerTableRow.builder()
                        .table(table)
                        .sourceId(String.valueOf(sourceId))
                        .build();

        return rowRepo.save(row);
    }


    @Transactional
    public void insertRowValues(
            ExternalPartnerTable table,
            ExternalPartnerTableRow row,
            OfflinePartnerInvite invite
    ) {
        List<ExternalPartnerTableColumn> columns =
                columnRepo.findByTableAndVisibleTrueOrderByDisplayOrderAsc(table);

        for (ExternalPartnerTableColumn column : columns) {

            String value = resolveValue(column.getName(), invite);
            if (value == null) continue;

            ExternalPartnerColumnValue columnValue =
                    ExternalPartnerColumnValue.builder()
                            .row(row)
                            .column(column)
                            .value(value)
                            .build();

            valueRepo.save(columnValue);
        }
    }


    private String resolveValue(String columnName, OfflinePartnerInvite invite) {

        return switch (columnName) {
            case "Poc Email" -> invite.getEmail();
            case "Status" -> invite.getStatus();
            case "Remarks" -> invite.getRemarks();
            case "Partner Group" -> "Default";
            case "Company Name" -> invite.getPartnerName();
            case "Verify Email Sent" -> "NO";
            case "Message Code" -> invite.getOfflinePartnerMessageCode();
            case "Is Member" -> String.valueOf(invite.isMember());
            default -> null;
        };
    }

    public List<ExternalPartnerTableColumn> getColumnsByOrgId(Long orgId) {

        ExternalPartnerTable table = tableRepo.findByOrgId(orgId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "External partner table not found for orgId: " + orgId
                ));

        return columnRepo.findByTableOrderByDisplayOrderAsc(table);
    }

    @Transactional
    public OrgColumnsResponse getAllColumnsForOrg() {
        var orgId = Util.getOrgIdFromToken();

        // ExternalDocColumns (DynamicTable columns)
        List<String> externalDocColumns = dynamicTableService.getColumnNamesForCurrentOrg();


        // ExternalPartnerColumns
        ExternalPartnerTable partnerTable =
                tableRepo.findByOrgId(orgId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "External partner table not found for orgId: " + orgId
                                )
                        );

        List<ExternalPartnerTableColumn> externalPartnerColumns =
                columnRepo
                        .findByTableAndVisibleTrueOrderByDisplayOrderAsc(partnerTable);

        return OrgColumnsResponse.builder()
                .externalDocColumns(externalDocColumns)
                .externalPartnerColumns(externalPartnerColumns)
                .build();
    }



}


package com.sharkdom.tablecustomization.service.overlaprecordfieldentityservice;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.entity.ai.OverlapRecordEntity;
import com.sharkdom.entity.ai.OverlapRecordFieldEntity;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.model.ai.RecordType;
import com.sharkdom.offlinePartner.entity.ColumnType;
import com.sharkdom.repository.ai.OverlapRecordsRepository;
import com.sharkdom.tablecustomization.dto.externalpartner.*;
import com.sharkdom.tablecustomization.entity.overlaprecordfieldentity.OverlapRecordFieldEntityColumnValue;
import com.sharkdom.tablecustomization.entity.overlaprecordfieldentity.OverlapRecordFieldEntityRow;
import com.sharkdom.tablecustomization.entity.overlaprecordfieldentity.OverlapRecordFieldEntityTable;
import com.sharkdom.tablecustomization.entity.overlaprecordfieldentity.OverlapRecordFieldEntityTableColumn;
import com.sharkdom.tablecustomization.repository.overlaprecordfieldentity.OverlapRecordFieldEntityTableColumnRepository;
import com.sharkdom.tablecustomization.repository.overlaprecordfieldentity.OverlapRecordFieldEntityTableColumnValueRepository;
import com.sharkdom.tablecustomization.repository.overlaprecordfieldentity.OverlapRecordFieldEntityTableRepository;
import com.sharkdom.tablecustomization.repository.overlaprecordfieldentity.OverlapRecordFieldEntityTableRowRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OverlapRecordFieldEntityTableColumnService {

    private final OverlapRecordFieldEntityTableRepository tableRepo;
    private final OverlapRecordFieldEntityTableColumnRepository columnRepo;
    private final OverlapRecordsRepository overlapRecordRepository;
    private final OverlapRecordFieldEntityTableColumnValueRepository valueRepo;
    private final OverlapRecordFieldEntityTableRowRepository rowRepo;

    // ============================
    // CREATE COLUMN
    // ============================
    public OverlapRecordFieldEntityTableColumn createColumn(
            CreateColumnExternalPartnerRequest request
    ) {

        OverlapRecordFieldEntityTable table =
                tableRepo.findById(request.getTableId())
                        .orElseThrow(() ->
                                new ServiceException(
                                        ErrorMessages.NOT_FOUND,
                                        "Table not found: " + request.getTableId()
                                )
                        );

        if (columnRepo.existsByTableAndNameIgnoreCaseAndVisibleTrue(
                table, request.getName())) {

            throw new ServiceException(
                    ErrorMessages.SH161,
                    request.getName()
            );
        }

        int nextOrder =
                columnRepo.findByTableAndVisibleTrueOrderByDisplayOrderAsc(table)
                        .size() + 1;

        OverlapRecordFieldEntityTableColumn column =
                OverlapRecordFieldEntityTableColumn.builder()
                        .table(table)
                        .name(request.getName())
                        .type(ColumnType.valueOf(request.getType()))
                        .displayOrder(nextOrder)
                        .visible(true)
                        .build();

        column = columnRepo.save(column);

        log.info(
                "Overlap Column CREATED → id={}, name={}, table={}",
                column.getId(),
                column.getName(),
                table.getId()
        );

        return column;
    }

    // ============================
    // UPDATE COLUMN ORDER
    // ============================
    public void updateColumnOrder(UpdateColumnOrderRequest request) {

        OverlapRecordFieldEntityTableColumn column =
                columnRepo.findById(request.getColumnId())
                        .orElseThrow(() ->
                                new ServiceException(
                                        ErrorMessages.NOT_FOUND,
                                        "Column not found: " + request.getColumnId()
                                )
                        );

        OverlapRecordFieldEntityTable table = column.getTable();

        var columns =
                columnRepo.findByTableAndVisibleTrueOrderByDisplayOrderAsc(table);

        if (request.getNewOrder() < 1 ||
                request.getNewOrder() > columns.size()) {

            throw new ServiceException(ErrorMessages.SH106);
        }

        int oldOrder = column.getDisplayOrder();
        int newOrder = request.getNewOrder();

        for (OverlapRecordFieldEntityTableColumn col : columns) {

            if (col.getId().equals(column.getId()))
                continue;

            if (oldOrder < newOrder) {

                if (col.getDisplayOrder() > oldOrder &&
                        col.getDisplayOrder() <= newOrder) {

                    col.setDisplayOrder(col.getDisplayOrder() - 1);
                }

            } else {

                if (col.getDisplayOrder() < oldOrder &&
                        col.getDisplayOrder() >= newOrder) {

                    col.setDisplayOrder(col.getDisplayOrder() + 1);
                }
            }
        }

        column.setDisplayOrder(newOrder);

        columnRepo.saveAll(columns);
        columnRepo.save(column);

        log.info(
                "Overlap Column ORDER UPDATED → id={}, newOrder={}",
                column.getId(),
                newOrder
        );
    }

    // ============================
    // RENAME COLUMN
    // ============================
    public void renameColumn(RenameColumnRequest request) {

        OverlapRecordFieldEntityTableColumn column =
                columnRepo.findById(request.getColumnId())
                        .orElseThrow(() ->
                                new ServiceException(
                                        ErrorMessages.NOT_FOUND,
                                        "Column not found: " + request.getColumnId()
                                )
                        );

        OverlapRecordFieldEntityTable table = column.getTable();

        if (columnRepo.existsByTableAndNameIgnoreCaseAndVisibleTrue(
                table,
                request.getNewName()
        )) {

            throw new ServiceException(
                    ErrorMessages.SH161,
                    request.getNewName()
            );
        }

        String oldName = column.getName();

        column.setName(request.getNewName());

        columnRepo.save(column);

        log.info(
                "Overlap Column RENAMED → id={}, '{}' → '{}'",
                column.getId(),
                oldName,
                request.getNewName()
        );
    }

    // ============================
    // SOFT DELETE COLUMN
    // ============================
    public void softDeleteColumn(Long columnId) {

        OverlapRecordFieldEntityTableColumn column =
                columnRepo.findById(columnId)
                        .orElseThrow(() ->
                                new ServiceException(
                                        ErrorMessages.NOT_FOUND,
                                        "Column not found: " + columnId
                                )
                        );

        column.setVisible(false);

        columnRepo.save(column);

        log.warn(
                "Overlap Column SOFT-DELETED → id={}, name={}",
                column.getId(),
                column.getName()
        );
    }

    @Transactional
    public List<OverlapRecordFieldEntityTable> generateTablesForOrganizations() {

        List<Long> orgIds = overlapRecordRepository.findDistinctOrganizationIds();

        List<OverlapRecordFieldEntityTable> savedTables = new ArrayList<>();

        for (Long orgId : orgIds) {

            // skip if already exists
            if (tableRepo.findByOrgId(orgId).isPresent()) {
                continue;
            }

            String tableName = generateCommonTableName(orgId);

            OverlapRecordFieldEntityTable entity =
                    OverlapRecordFieldEntityTable.builder()
                            .orgId(orgId)
                            .tableName(tableName)
                            .build();

            savedTables.add(tableRepo.save(entity));
        }

        return savedTables;
    }

    private String generateCommonTableName(Long orgId) {
        return "overlap_record_org_" + orgId;
    }

    @Transactional
    public List<OverlapRecordFieldEntityTableColumn> createDefaultColumnsForAllTables() {

        List<OverlapRecordFieldEntityTable> tables = tableRepo.findAll();

        if (tables.isEmpty()) {
            throw new ServiceException(
                    ErrorMessages.NOT_FOUND,
                    "No dynamic overlap record tables found"
            );
        }

        List<OverlapRecordFieldEntityTableColumn> createdColumns = new ArrayList<>();

        // default schema
        List<ColumnDefinition> defaultColumns = List.of(
                new ColumnDefinition("name", ColumnType.TEXT),
                new ColumnDefinition("companyName", ColumnType.TEXT),
                new ColumnDefinition("contactEmail", ColumnType.TEXT),
                new ColumnDefinition("domain", ColumnType.TEXT),
                new ColumnDefinition("dealStage", ColumnType.TEXT),
                new ColumnDefinition("creationDate", ColumnType.DATE),
                new ColumnDefinition("closeDate", ColumnType.DATE),
                new ColumnDefinition("subscribed", ColumnType.TEXT),
                new ColumnDefinition("ticketSize", ColumnType.NUMBER),
                new ColumnDefinition("recordType", ColumnType.TEXT)
        );

        for (OverlapRecordFieldEntityTable table : tables) {

            int order = 1;

            for (ColumnDefinition def : defaultColumns) {

                boolean exists =
                        columnRepo.existsByTableAndNameIgnoreCaseAndVisibleTrue(
                                table,
                                def.name()
                        );

                if (exists) {
                    continue;
                }

                OverlapRecordFieldEntityTableColumn column =
                        OverlapRecordFieldEntityTableColumn.builder()
                                .name(def.name())
                                .type(def.type())
                                .displayOrder(order++)
                                .visible(true)
                                .table(table)
                                .build();

                column = columnRepo.save(column);

                createdColumns.add(column);

                log.info(
                        "Column CREATED → orgId={}, table={}, column={}",
                        table.getOrgId(),
                        table.getTableName(),
                        column.getName()
                );
            }
        }

        return createdColumns;
    }

    private record ColumnDefinition(String name, ColumnType type) {}

    @Transactional
    public void migrateOverlapRecordDataToDynamicTable() {

        List<OverlapRecordEntity> overlapRecords = overlapRecordRepository.findAll();

        for (OverlapRecordEntity record : overlapRecords) {

            Long orgId = record.getOrganizationId();
            RecordType recordType = record.getRecordType();

            // Step 1: Find dynamic table for org
            OverlapRecordFieldEntityTable table =
                    tableRepo.findByOrgId(orgId)
                            .orElseThrow(() ->
                                    new ServiceException(
                                            ErrorMessages.NOT_FOUND,
                                            "Dynamic table not found for orgId: " + orgId
                                    ));

            // Step 2: Get columns
            List<OverlapRecordFieldEntityTableColumn> columns =
                    columnRepo.findByTableIdAndVisibleTrue(table.getId());

            Map<String, OverlapRecordFieldEntityTableColumn> columnMap =
                    columns.stream()
                            .collect(Collectors.toMap(
                                    OverlapRecordFieldEntityTableColumn::getName,
                                    c -> c
                            ));

            // Step 3: Get fields
            List<OverlapRecordFieldEntity> fields = record.getFields();

            for (OverlapRecordFieldEntity field : fields) {

                // Step 4: Create Row (unique sourceId = overlapRecordFieldEntity.id)
                OverlapRecordFieldEntityRow row =
                        rowRepo.findByTableIdAndSourceId(
                                        table.getId(),
                                        field.getId().toString()
                                )
                                .orElseGet(() -> rowRepo.save(
                                        OverlapRecordFieldEntityRow.builder()
                                                .table(table)
                                                .sourceId(field.getId().toString())
                                                .build()
                                ));

                // Step 5: Save column values
                saveColumnValue(row, columnMap.get("name"), field.getName());
                saveColumnValue(row, columnMap.get("companyName"), field.getCompanyName());
                saveColumnValue(row, columnMap.get("contactEmail"), field.getContactEmail());
                saveColumnValue(row, columnMap.get("domain"), field.getDomain());
                saveColumnValue(row, columnMap.get("dealStage"), field.getDealStage());
                saveColumnValue(row, columnMap.get("creationDate"), field.getCreationDate());
                saveColumnValue(row, columnMap.get("closeDate"), field.getCloseDate());
                saveColumnValue(row, columnMap.get("subscribed"), field.getSubscribed());
                saveColumnValue(row, columnMap.get("ticketSize"), field.getTicketSize());

                // recordType column
                saveColumnValue(row, columnMap.get("recordType"), recordType.name());
            }

            log.info("Migration done → orgId={}, recordType={}", orgId, recordType);
        }
    }

    private void saveColumnValue(
            OverlapRecordFieldEntityRow row,
            OverlapRecordFieldEntityTableColumn column,
            String value
    ) {

        if (column == null || value == null)
            return;

        boolean exists =
                valueRepo.existsByRowIdAndColumnId(
                        row.getId(),
                        column.getId()
                );

        if (exists)
            return;

        OverlapRecordFieldEntityColumnValue entity =
                OverlapRecordFieldEntityColumnValue.builder()
                        .row(row)
                        .column(column)
                        .value(value)
                        .build();

        valueRepo.save(entity);
    }

    @Transactional
    public void upsertOverlapRowValues(UpdateRowValuesRequest request) {

        if (request.getRowId() == null) {
            throw new ServiceException(
                    ErrorMessages.NOT_FOUND,
                    "rowId is required"
            );
        }

        if (request.getValues() == null || request.getValues().isEmpty()) {
            log.warn("No values provided → rowId={}", request.getRowId());
            return;
        }

        // ============================
        // STEP 1: Fetch Row
        // ============================
        OverlapRecordFieldEntityRow row =
                rowRepo.findById(request.getRowId())
                        .orElseThrow(() ->
                                new ServiceException(
                                        ErrorMessages.NOT_FOUND,
                                        "Row not found: " + request.getRowId()
                                ));


        // ============================
        // STEP 2: Fetch all columns in one query
        // ============================
        List<Long> columnIds =
                new ArrayList<>(request.getValues().keySet());

        List<OverlapRecordFieldEntityTableColumn> columns =
                columnRepo.findAllById(columnIds);

        Map<Long, OverlapRecordFieldEntityTableColumn> columnMap =
                columns.stream()
                        .collect(Collectors.toMap(
                                OverlapRecordFieldEntityTableColumn::getId,
                                Function.identity()
                        ));


        // ============================
        // STEP 3: Fetch existing cells in one query
        // ============================
        List<OverlapRecordFieldEntityColumnValue> existingCells =
                valueRepo.findByRowIdAndColumnIdIn(
                        row.getId(),
                        columnIds
                );

        Map<Long, OverlapRecordFieldEntityColumnValue> existingCellMap =
                existingCells.stream()
                        .collect(Collectors.toMap(
                                cell -> cell.getColumn().getId(),
                                Function.identity()
                        ));


        // ============================
        // STEP 4: Prepare batch save
        // ============================
        List<OverlapRecordFieldEntityColumnValue> cellsToSave =
                new ArrayList<>();

        for (Map.Entry<Long, String> entry : request.getValues().entrySet()) {

            Long columnId = entry.getKey();
            String value = entry.getValue();

            OverlapRecordFieldEntityTableColumn column =
                    columnMap.get(columnId);

            if (column == null) {
                throw new ServiceException(
                        ErrorMessages.NOT_FOUND,
                        "Column not found: " + columnId
                );
            }

            OverlapRecordFieldEntityColumnValue cell =
                    existingCellMap.get(columnId);

            if (cell == null) {

                cell = OverlapRecordFieldEntityColumnValue.builder()
                        .row(row)
                        .column(column)
                        .build();
            }

            cell.setValue(value);

            cellsToSave.add(cell);
        }


        // ============================
        // STEP 5: Batch Save
        // ============================
        valueRepo.saveAll(cellsToSave);


        // ============================
        // LOG
        // ============================
        log.info(
                "OVERLAP ROW UPSERTED → rowId={}, updatedCells={}",
                row.getId(),
                cellsToSave.size()
        );
    }

    @Transactional
    public ExternalPartnerTableResponse getOverlapTableData(
            Long orgId,
            RecordType recordType
    ) {

        // STEP 1: table
        OverlapRecordFieldEntityTable table =
                tableRepo.findByOrgId(orgId)
                        .orElseThrow(() ->
                                new ServiceException(
                                        ErrorMessages.NOT_FOUND,
                                        "Table not found"
                                ));

        // STEP 2: columns
        List<OverlapRecordFieldEntityTableColumn> columns =
                columnRepo.findByTableIdAndVisibleTrueOrderByDisplayOrderAsc(
                        table.getId());

        Map<Long, ColumnResponse> columnResponseMap =
                columns.stream()
                        .collect(Collectors.toMap(
                                OverlapRecordFieldEntityTableColumn::getId,
                                col -> ColumnResponse.builder()
                                        .columnId(col.getId())
                                        .name(col.getName())
                                        .type(col.getType())
                                        .displayOrder(col.getDisplayOrder())
                                        .visible(col.getVisible())
                                        .build()
                        ));

        // STEP 3: recordType columnId
        Long recordTypeColumnId =
                columns.stream()
                        .filter(c -> c.getName().equals("recordType"))
                        .findFirst()
                        .orElseThrow()
                        .getId();


        // STEP 4: find rows by recordType
        List<Long> rowIds =
                valueRepo.findRowIdsByColumnIdAndValue(
                        recordTypeColumnId,
                        recordType.name()
                );


        if (rowIds.isEmpty()) {

            return ExternalPartnerTableResponse.builder()
                    .tableId(table.getId())
                    .tableName(table.getTableName())
                    .orgId(orgId)
                    .columns(new ArrayList<>(columnResponseMap.values()))
                    .rows(new ArrayList<>())
                    .build();
        }


        // STEP 5: fetch values
        List<OverlapRecordFieldEntityColumnValue> values =
                valueRepo.findByRowIdIn(rowIds);

        Map<Long, Map<Long, String>> rowValueMap = new HashMap<>();

        for (OverlapRecordFieldEntityColumnValue v : values) {

            rowValueMap
                    .computeIfAbsent(v.getRow().getId(), k -> new HashMap<>())
                    .put(v.getColumn().getId(), v.getValue());
        }


        // STEP 6: build rows
        List<RowResponse> rows =
                rowIds.stream()
                        .map(rowId -> RowResponse.builder()
                                .rowId(rowId)
                                .values(rowValueMap.get(rowId))
                                .build())
                        .toList();


        return ExternalPartnerTableResponse.builder()
                .tableId(table.getId())
                .tableName(table.getTableName())
                .orgId(orgId)
                .columns(new ArrayList<>(columnResponseMap.values()))
                .rows(rows)
                .build();
    }

    @Transactional
    public void syncOverlapRecordToDynamicTable(OverlapRecordEntity record) {

        Long orgId = record.getOrganizationId();
        RecordType recordType = record.getRecordType();

        // STEP 1: Ensure table exists
        OverlapRecordFieldEntityTable table =
                tableRepo.findByOrgId(orgId)
                        .orElseGet(() -> {

                            OverlapRecordFieldEntityTable newTable =
                                    OverlapRecordFieldEntityTable.builder()
                                            .orgId(orgId)
                                            .tableName("overlap_record_org_" + orgId)
                                            .build();

                            return tableRepo.save(newTable);
                        });

        // STEP 2: Ensure default columns exist
        createDefaultColumnsForTable(table);

        // STEP 3: Fetch columns
        List<OverlapRecordFieldEntityTableColumn> columns =
                columnRepo.findByTableIdAndVisibleTrue(table.getId());

        Map<String, OverlapRecordFieldEntityTableColumn> columnMap =
                columns.stream()
                        .collect(Collectors.toMap(
                                OverlapRecordFieldEntityTableColumn::getName,
                                c -> c
                        ));

        // STEP 4: Process fields
        for (OverlapRecordFieldEntity field : record.getFields()) {

            // unique row per fieldId
            OverlapRecordFieldEntityRow row =
                    rowRepo.findByTableIdAndSourceId(
                                    table.getId(),
                                    field.getId().toString()
                            )
                            .orElseGet(() ->
                                    rowRepo.save(
                                            OverlapRecordFieldEntityRow.builder()
                                                    .table(table)
                                                    .sourceId(field.getId().toString())
                                                    .build()
                                    )
                            );

            saveOrUpdate(row, columnMap.get("name"), field.getName());
            saveOrUpdate(row, columnMap.get("companyName"), field.getCompanyName());
            saveOrUpdate(row, columnMap.get("contactEmail"), field.getContactEmail());
            saveOrUpdate(row, columnMap.get("domain"), field.getDomain());
            saveOrUpdate(row, columnMap.get("dealStage"), field.getDealStage());
            saveOrUpdate(row, columnMap.get("creationDate"), field.getCreationDate());
            saveOrUpdate(row, columnMap.get("closeDate"), field.getCloseDate());
            saveOrUpdate(row, columnMap.get("subscribed"), field.getSubscribed());
            saveOrUpdate(row, columnMap.get("ticketSize"), field.getTicketSize());

            saveOrUpdate(row, columnMap.get("recordType"), recordType.name());
        }

        log.info("Dynamic table synced for orgId={}", orgId);
    }


    private void saveOrUpdate(
            OverlapRecordFieldEntityRow row,
            OverlapRecordFieldEntityTableColumn column,
            String value
    ) {

        if (column == null || value == null)
            return;

        OverlapRecordFieldEntityColumnValue cell =
                valueRepo.findByRowIdAndColumnId(
                        row.getId(),
                        column.getId()
                ).orElse(null);

        if (cell == null) {

            cell = OverlapRecordFieldEntityColumnValue.builder()
                    .row(row)
                    .column(column)
                    .value(value)
                    .build();

        } else {

            cell.setValue(value);
        }

        valueRepo.save(cell);
    }

    private void createDefaultColumnsForTable(OverlapRecordFieldEntityTable table) {

        List<ColumnDefinition> defaultColumns = List.of(
                new ColumnDefinition("name", ColumnType.TEXT),
                new ColumnDefinition("companyName", ColumnType.TEXT),
                new ColumnDefinition("contactEmail", ColumnType.TEXT),
                new ColumnDefinition("domain", ColumnType.TEXT),
                new ColumnDefinition("dealStage", ColumnType.TEXT),
                new ColumnDefinition("creationDate", ColumnType.DATE),
                new ColumnDefinition("closeDate", ColumnType.DATE),
                new ColumnDefinition("subscribed", ColumnType.TEXT),
                new ColumnDefinition("ticketSize", ColumnType.NUMBER),
                new ColumnDefinition("recordType", ColumnType.TEXT)
        );

        int order = 1;

        for (ColumnDefinition def : defaultColumns) {

            boolean exists =
                    columnRepo.existsByTableAndNameIgnoreCaseAndVisibleTrue(
                            table,
                            def.name()
                    );

            if (!exists) {

                OverlapRecordFieldEntityTableColumn column =
                        OverlapRecordFieldEntityTableColumn.builder()
                                .table(table)
                                .name(def.name())
                                .type(def.type())
                                .displayOrder(order++)
                                .visible(true)
                                .build();

                columnRepo.save(column);
            }
        }
    }

}
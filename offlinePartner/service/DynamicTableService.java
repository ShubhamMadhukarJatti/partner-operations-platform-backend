package com.sharkdom.offlinePartner.service;

import com.sharkdom.offlinePartner.entity.*;
import com.sharkdom.offlinePartner.model.OfflinePartnerDocumentRequest;
import com.sharkdom.offlinePartner.repository.*;
import com.sharkdom.util.Util;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicTableService {

    private final DynamicTableRepository tableRepo;
    private final TableColumnRepository columnRepo;
    private final TableRowRepository rowRepo;
    private final ColumnValueRepository valueRepo;
    private final TagOptionRepository tagRepo;


    public DynamicTable createTable(Long orgId, String email, String name) {
        return tableRepo.save(
                DynamicTable.builder()
                        .orgId(orgId)
                        .email(email)
                        .name(name)
                        .build()
        );
    }

    public TableColumn createColumn(Long tableId, String name, ColumnType type, Boolean visible) {
        DynamicTable table = tableRepo.findById(tableId).orElseThrow();
        return columnRepo.save(
                TableColumn.builder()
                        .name(name)
                        .type(type)
                        .visible(visible != null ? visible : true)
                        .table(table)
                        .build()
        );
    }

    public TableRow createRow(Long tableId, String title) {
        DynamicTable table = tableRepo.findById(tableId).orElseThrow();
        return rowRepo.save(
                TableRow.builder()
                        .table(table)
                        .build()
        );
    }

    @Transactional
    public void updateColumnVisibility(Long columnId, Boolean visible) {
        TableColumn col = columnRepo.findById(columnId).orElseThrow();
        col.setVisible(visible);
    }

    public TagOption addTagOption(Long columnId, String label, String color) {
        return tagRepo.save(
                TagOption.builder()
                        .column(columnRepo.findById(columnId).orElseThrow())
                        .label(label)
                        .color(color)
                        .build()
        );
    }

    public List<TagOption> getTagOptions(Long columnId) {
        return tagRepo.findByColumnId(columnId);
    }

    void saveColumnValues(
            TableRow row,
            OfflinePartnerDocumentRequest request,
            String pdfLink
    ) {
        List<TableColumn> columns =
                columnRepo.findByTableIdOrderByDisplayOrderAsc(
                        row.getTable().getId()
                );

        for (TableColumn column : columns) {

            String value = switch (column.getName()) {
                case "email" -> request.getEmail();
                case "organizationId" -> String.valueOf(request.getOrganizationId());
                case "pdfUrl" -> pdfLink;
                case "docId" -> request.getDocId();
                case "count" -> String.valueOf(request.getCount());
                case "effectiveDate" -> request.getEffectiveDate();
                case "expiringDate" -> request.getExpiringDate();
                default -> null;
            };

            if (value != null) {
                valueRepo.save(
                        ColumnValue.builder()
                                .row(row)
                                .column(column)
                                .value(value)
                                .build()
                );
            }
        }
    }

    private void createColumnIfMissing(
            DynamicTable table,
            Set<String> existingNames,
            String columnName,
            ColumnType type
    ) {
        if (!existingNames.contains(columnName)) {
            columnRepo.save(
                    TableColumn.builder()
                            .name(columnName)
                            .type(type)
                            .visible(true)
                            .table(table)
                            .displayOrder(0)
                            .build()
            );
        }
    }

    void ensureDefaultColumns(DynamicTable table) {

        List<TableColumn> existing =
                columnRepo.findByTableId(table.getId());

        Set<String> existingNames = existing.stream()
                .map(TableColumn::getName)
                .collect(Collectors.toSet());

        createColumnIfMissing(table, existingNames, "email", ColumnType.TEXT);
        createColumnIfMissing(table, existingNames, "organizationId", ColumnType.NUMBER);
        createColumnIfMissing(table, existingNames, "pdfUrl", ColumnType.TEXT);
        createColumnIfMissing(table, existingNames, "docId", ColumnType.TEXT);
        createColumnIfMissing(table, existingNames, "count", ColumnType.NUMBER);
        createColumnIfMissing(table, existingNames, "effectiveDate", ColumnType.DATE);
        createColumnIfMissing(table, existingNames, "expiringDate", ColumnType.DATE);
    }

    @Transactional
    public TableColumn createColumn(
            Long tableId,
            String email,
            CreateColumnRequest request
    ) {
        Long orgId = Util.getOrgIdFromToken();

        DynamicTable table = tableRepo
                .findByIdAndOrgIdAndEmail(tableId, orgId, email)
                .orElseThrow(() ->
                        new RuntimeException("Dynamic table not found")
                );

        boolean exists = columnRepo
                .existsByTableIdAndName(tableId, request.getName());

        if (exists) {
            throw new RuntimeException("Column already exists");
        }

        TableColumn column = TableColumn.builder()
                .table(table)
                .name(request.getName())
                .type(request.getType())
                .displayOrder(request.getDisplayOrder())
                .visible(request.getVisible())
                .build();

        return columnRepo.save(column);
    }

    @Transactional
    public TableColumn updateColumnVisibility(
            Long tableId,
            Long columnId,
            String email,
            Boolean visible
    ) {
        Long orgId = Util.getOrgIdFromToken();

        DynamicTable table = tableRepo
                .findByIdAndOrgIdAndEmail(tableId, orgId, email)
                .orElseThrow(() ->
                        new RuntimeException("Dynamic table not found")
                );

        TableColumn column = columnRepo
                .findByIdAndTableId(columnId, table.getId())
                .orElseThrow(() ->
                        new RuntimeException("Column not found")
                );

        column.setVisible(visible);
        return columnRepo.save(column);
    }

    public DynamicTableResponse getTableDataByEmail(String email) {

        Long orgId = Util.getOrgIdFromToken();

        DynamicTable table = tableRepo
                .findByOrgIdAndEmail(orgId, email)
                .orElseThrow(() ->
                        new RuntimeException("Dynamic table not found")
                );

        List<TableColumn> columns =
                columnRepo.findByTableIdOrderByDisplayOrderAsc(
                        table.getId()
                );

        List<TableRow> rows =
                rowRepo.findByTableId(table.getId());

        List<ColumnValue> values =
                valueRepo.findByTableId(table.getId());

        Map<Long, Map<String, String>> rowCellMap = new HashMap<>();

        for (ColumnValue value : values) {
            rowCellMap
                    .computeIfAbsent(
                            value.getRow().getId(),
                            k -> new HashMap<>()
                    )
                    .put(
                            value.getColumn().getName(),
                            value.getValue()
                    );
        }

        DynamicTableResponse response = new DynamicTableResponse();
        response.setTableId(table.getId());
        response.setTableName(table.getName());

        response.setColumns(
                columns.stream().map(c -> {
                    ColumnDto dto = new ColumnDto();
                    dto.setId(c.getId());
                    dto.setName(c.getName());
                    dto.setVisible(c.getVisible());
                    return dto;
                }).collect(Collectors.toList())
        );

        response.setRows(
                rows.stream().map(r -> {
                    RowDto dto = new RowDto();
                    dto.setRowId(r.getId());
                    dto.setCells(
                            rowCellMap.getOrDefault(
                                    r.getId(),
                                    Collections.emptyMap()
                            )
                    );
                    return dto;
                }).collect(Collectors.toList())
        );

        return response;
    }

    @Transactional
    public void saveRowValues(
            Long tableId,
            Long rowId,
            String email,
            SaveRowValuesRequest request
    ) {
        Long orgId = Util.getOrgIdFromToken();

        DynamicTable table = tableRepo
                .findByIdAndOrgIdAndEmail(tableId, orgId, email)
                .orElseThrow(() ->
                        new RuntimeException("Table not found")
                );

        TableRow row = rowRepo
                .findByIdAndTableId(rowId, table.getId())
                .orElseThrow(() ->
                        new RuntimeException("Row not found")
                );

        for (var entry : request.getCells().entrySet()) {

            Long columnId = entry.getKey();
            String value = entry.getValue();

            TableColumn column = columnRepo
                    .findByIdAndTableId(columnId, table.getId())
                    .orElseThrow(() ->
                            new RuntimeException("Invalid column")
                    );

            ColumnValue columnValue =
                    valueRepo
                            .findByRowIdAndColumnId(row.getId(), column.getId())
                            .orElseGet(() ->
                                    ColumnValue.builder()
                                            .row(row)
                                            .column(column)
                                            .build()
                            );

            columnValue.setValue(value);
            valueRepo.save(columnValue);
        }
    }

    @Transactional
    public TableColumn updateColumnName(
            Long tableId,
            Long columnId,
            String email,
            String newName
    ) {
        Long orgId = Util.getOrgIdFromToken();

        DynamicTable table = tableRepo
                .findByIdAndOrgIdAndEmail(tableId, orgId, email)
                .orElseThrow(() ->
                        new RuntimeException("Dynamic table not found")
                );

        if (columnRepo.existsByTableIdAndName(tableId, newName)) {
            throw new RuntimeException("Column name already exists");
        }

        TableColumn column = columnRepo
                .findByIdAndTableId(columnId, table.getId())
                .orElseThrow(() ->
                        new RuntimeException("Column not found")
                );

        column.setName(newName.trim());
        return columnRepo.save(column);
    }

    @Transactional
    public void deleteColumn(
            Long tableId,
            Long columnId,
            String email
    ) {
        Long orgId = Util.getOrgIdFromToken();

        DynamicTable table = tableRepo
                .findByIdAndOrgIdAndEmail(tableId, orgId, email)
                .orElseThrow(() ->
                        new RuntimeException("Dynamic table not found")
                );

        TableColumn column = columnRepo
                .findByIdAndTableId(columnId, table.getId())
                .orElseThrow(() ->
                        new RuntimeException("Column not found")
                );

        valueRepo.deleteByColumnId(column.getId());
        columnRepo.delete(column);
    }

    @Transactional
    public List<TableColumn> updateColumnOrder(
            Long tableId,
            String email,
            List<UpdateColumnOrderRequest.ColumnOrder> columnOrders
    ) {
        Long orgId = Util.getOrgIdFromToken();

        DynamicTable table = tableRepo
                .findByIdAndOrgIdAndEmail(tableId, orgId, email)
                .orElseThrow(() ->
                        new RuntimeException("Dynamic table not found")
                );

        List<TableColumn> columns = columnRepo
                .findByTableId(table.getId());

        Map<Long, TableColumn> columnMap = columns.stream()
                .collect(Collectors.toMap(TableColumn::getId, c -> c));

        for (UpdateColumnOrderRequest.ColumnOrder order : columnOrders) {
            TableColumn column = columnMap.get(order.getColumnId());

            if (column == null) {
                throw new RuntimeException(
                        "Column not found in this table: " + order.getColumnId()
                );
            }

            column.setDisplayOrder(order.getDisplayOrder());
        }

        return columnRepo.saveAll(columns);
    }

    @Transactional
    public List<String> getColumnNamesForCurrentOrg() {

        Long orgId = Util.getOrgIdFromToken();

        List<DynamicTable> tables = tableRepo.findByOrgId(orgId);

        if (tables.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> tableIds = tables.stream()
                .map(DynamicTable::getId)
                .toList();

        return columnRepo.findByTableIdIn(tableIds)
                .stream()
                .map(TableColumn::getName)
                .distinct()
                .sorted()
                .toList();
    }



}

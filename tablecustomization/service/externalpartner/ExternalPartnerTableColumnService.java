package com.sharkdom.tablecustomization.service.externalpartner;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.offlinePartner.entity.ColumnType;
import com.sharkdom.tablecustomization.dto.externalpartner.*;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExternalPartnerTableColumnService {

    private final ExternalPartnerTableRepository tableRepo;
    private final ExternalPartnerTableColumnRepository columnRepo;
    private final ExternalPartnerTableRowRepository rowRepo;
    private final ExternalPartnerTableColumnValueRepository valueRepo;

    // ============================
    // CREATE COLUMN
    // ============================
    public ExternalPartnerTableColumn createColumn(CreateColumnExternalPartnerRequest request) {

        ExternalPartnerTable table = tableRepo.findById(request.getTableId())
                .orElseThrow(() ->
                        new ServiceException(ErrorMessages.NOT_FOUND,
                                "Table not found: " + request.getTableId()));

        if (columnRepo.existsByTableAndNameIgnoreCaseAndVisibleTrue(table, request.getName())) {
            throw new ServiceException(ErrorMessages.SH161, request.getName());
        }

        int nextOrder = columnRepo
                .findByTableAndVisibleTrueOrderByDisplayOrderAsc(table)
                .size() + 1;

        ExternalPartnerTableColumn column = ExternalPartnerTableColumn.builder()
                .table(table)
                .name(request.getName())
                .type(ColumnType.valueOf(request.getType()))
                .displayOrder(nextOrder)
                .visible(true)
                .build();

        column = columnRepo.save(column);

        log.info("Column CREATED → id={}, name={}, table={}",
                column.getId(), column.getName(), table.getId());

        return column;
    }

    // ============================
    // UPDATE COLUMN ORDER
    // ============================
    public void updateColumnOrder(UpdateColumnOrderRequest request) {

        ExternalPartnerTableColumn column = columnRepo.findById(request.getColumnId())
                .orElseThrow(() ->
                        new ServiceException(ErrorMessages.NOT_FOUND,
                                "Column not found: " + request.getColumnId()));

        ExternalPartnerTable table = column.getTable();

        List<ExternalPartnerTableColumn> columns =
                columnRepo.findByTableAndVisibleTrueOrderByDisplayOrderAsc(table);

        if (request.getNewOrder() < 1 || request.getNewOrder() > columns.size()) {
            throw new ServiceException(ErrorMessages.SH106);
        }

        int oldOrder = column.getDisplayOrder();
        int newOrder = request.getNewOrder();

        for (ExternalPartnerTableColumn col : columns) {
            if (col.getId().equals(column.getId())) continue;

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

        log.info("Column ORDER UPDATED → id={}, newOrder={}",
                column.getId(), newOrder);
    }

    // ============================
    // RENAME COLUMN
    // ============================
    public void renameColumn(RenameColumnRequest request) {

        ExternalPartnerTableColumn column = columnRepo.findById(request.getColumnId())
                .orElseThrow(() ->
                        new ServiceException(ErrorMessages.NOT_FOUND,
                                "Column not found: " + request.getColumnId()));

        ExternalPartnerTable table = column.getTable();

        if (columnRepo.existsByTableAndNameIgnoreCaseAndVisibleTrue(
                table, request.getNewName())) {
            throw new ServiceException(ErrorMessages.SH161, request.getNewName());
        }

        String oldName = column.getName();
        column.setName(request.getNewName());

        columnRepo.save(column);

        log.info("Column RENAMED → id={}, '{}' → '{}'",
                column.getId(), oldName, request.getNewName());
    }

    // ============================
    // SOFT DELETE COLUMN
    // ============================
    public void softDeleteColumn(Long columnId) {

        ExternalPartnerTableColumn column = columnRepo.findById(columnId)
                .orElseThrow(() ->
                        new ServiceException(ErrorMessages.NOT_FOUND,
                                "Column not found: " + columnId));

        column.setVisible(false);

        columnRepo.save(column);

        log.warn("Column SOFT-DELETED → id={}, name={}",
                column.getId(), column.getName());
    }

    public void upsertRowValues(UpdateRowValuesRequest request) {

        ExternalPartnerTableRow row = rowRepo.findById(request.getRowId())
                .orElseThrow(() ->
                        new RuntimeException("Row not found: " + request.getRowId()));

        for (var entry : request.getValues().entrySet()) {

            Long columnId = entry.getKey();
            String value = entry.getValue();

            ExternalPartnerTableColumn column = columnRepo.findById(columnId)
                    .orElseThrow(() ->
                            new RuntimeException("Column not found: " + columnId));

            ExternalPartnerColumnValue cell =
                    valueRepo.findByRowAndColumn(row, column)
                            .orElseGet(() ->
                                    ExternalPartnerColumnValue.builder()
                                            .row(row)
                                            .column(column)
                                            .build()
                            );

            cell.setValue(value);
            valueRepo.save(cell);
        }

        log.info("Row VALUES UPDATED → row={}, cells={}",
                row.getId(), request.getValues().size());
    }

    public ExternalPartnerTableResponse getFullTableByOrgId(Long orgId) {

        ExternalPartnerTable table = tableRepo.findByOrgId(orgId)
                .orElseThrow(() ->
                        new RuntimeException("No table found for orgId: " + orgId));

        // ------------------
        // COLUMNS (VISIBLE ONLY)
        // ------------------
        List<ExternalPartnerTableColumn> columns =
                columnRepo.findByTableOrderByDisplayOrderAsc(table)
                        .stream()
                        .filter(ExternalPartnerTableColumn::getVisible) // <-- only visible
                        .toList();

        List<ColumnResponse> columnResponses = columns.stream()
                .map(col -> ColumnResponse.builder()
                        .columnId(col.getId())
                        .name(col.getName())
                        .type(col.getType())
                        .displayOrder(col.getDisplayOrder())
                        .visible(col.getVisible())
                        .build()
                )
                .toList();

        // ------------------
        // ROWS
        // ------------------
        List<ExternalPartnerTableRow> rows =
                rowRepo.findByTableOrderByIdAsc(table);

        // ------------------
        // VALUES (BULK LOAD, ONLY VISIBLE COLUMNS)
        // ------------------
        List<ExternalPartnerColumnValue> allValues =
                valueRepo.findByRowIn(rows)
                        .stream()
                        .filter(cell -> columns.contains(cell.getColumn())) // <-- only visible columns
                        .toList();

        // RowId -> (ColumnId -> Value)
        Map<Long, Map<Long, String>> rowValueMap = new HashMap<>();
        for (ExternalPartnerColumnValue cell : allValues) {
            rowValueMap
                    .computeIfAbsent(cell.getRow().getId(), k -> new HashMap<>())
                    .put(cell.getColumn().getId(), cell.getValue());
        }

        // ------------------
        // BUILD ROW RESPONSES
        // ------------------
        List<RowResponse> rowResponses = rows.stream()
                .map(row -> RowResponse.builder()
                        .rowId(row.getId())
                        .values(rowValueMap.getOrDefault(row.getId(), Map.of()))
                        .build()
                )
                .toList();

        log.info("FULL TABLE FETCHED → orgId={}, rows={}, visible columns={}",
                orgId, rows.size(), columns.size());

        // ------------------
        // FINAL RESPONSE
        // ------------------
        return ExternalPartnerTableResponse.builder()
                .tableId(table.getId())
                .tableName(table.getTableName())
                .orgId(table.getOrgId())
                .columns(columnResponses)
                .rows(rowResponses)
                .build();
    }


}


package com.sharkdom.tablecustomization.controller.overlaprecordfieldentity;

import com.sharkdom.model.ai.RecordType;
import com.sharkdom.tablecustomization.dto.externalpartner.*;
import com.sharkdom.tablecustomization.entity.overlaprecordfieldentity.OverlapRecordFieldEntityTable;
import com.sharkdom.tablecustomization.entity.overlaprecordfieldentity.OverlapRecordFieldEntityTableColumn;
import com.sharkdom.tablecustomization.service.overlaprecordfieldentityservice.OverlapRecordFieldEntityTableColumnService;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/Overlap/Field/entity/table")
@RequiredArgsConstructor
@Tag(name = "Overlap Record Field Entity Dynamic Table", description = "APIs for managing dynamic table, columns and row values")
public class OverlapRecordFieldEntityController {

    private final OverlapRecordFieldEntityTableColumnService service;

    // ============================
    // CREATE COLUMN
    // ============================
    @Operation(
            summary = "Create Column",
            description = "Creates a new visible column in the overlap record table"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Column created successfully"),
            @ApiResponse(responseCode = "404", description = "Table not found"),
            @ApiResponse(responseCode = "409", description = "Column already exists")
    })
    @PostMapping("/column")
    public ResponseEntity<SharkdomApiResponse<OverlapRecordFieldEntityTableColumn>> createColumn(
            @RequestBody CreateColumnExternalPartnerRequest request
    ) {

        OverlapRecordFieldEntityTableColumn column =
                service.createColumn(request);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Column created successfully",
                        column
                )
        );
    }

    // ============================
// UPDATE COLUMN ORDER
// ============================
    @Operation(
            summary = "Update Column Order",
            description = "Changes display order of a visible column"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Column order updated"),
            @ApiResponse(responseCode = "404", description = "Column not found"),
            @ApiResponse(responseCode = "400", description = "Invalid order value")
    })
    @PutMapping("/column/order")
    public ResponseEntity<SharkdomApiResponse<Void>> updateColumnOrder(
            @RequestBody UpdateColumnOrderRequest request
    ) {

        service.updateColumnOrder(request);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Column order updated successfully",
                        null
                )
        );
    }


    // ============================
    // RENAME COLUMN
    // ============================
    @Operation(
            summary = "Rename Column",
            description = "Renames an existing visible column"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Column renamed"),
            @ApiResponse(responseCode = "404", description = "Column not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate column name")
    })
    @PutMapping("/column/rename")
    public ResponseEntity<SharkdomApiResponse<Void>> renameColumn(
            @RequestBody RenameColumnRequest request
    ) {

        service.renameColumn(request);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Column renamed successfully",
                        null
                )
        );
    }


    // ============================
    // SOFT DELETE COLUMN
    // ============================
    @Operation(
            summary = "Soft Delete Column",
            description = "Marks a column as invisible instead of deleting it permanently"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Column soft deleted"),
            @ApiResponse(responseCode = "404", description = "Column not found")
    })
    @DeleteMapping("/column/{columnId}")
    public ResponseEntity<SharkdomApiResponse<Void>> softDeleteColumn(
            @PathVariable Long columnId
    ) {

        service.softDeleteColumn(columnId);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Column soft deleted successfully",
                        null
                )
        );
    }

    @Operation(
            summary = "Upsert Overlap Row Values",
            description = "Creates or updates values for overlap record row and its columns"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overlap row values updated"),
            @ApiResponse(responseCode = "404", description = "Row or Column not found")
    })
    @PutMapping("/overlap/row/values")
    public ResponseEntity<SharkdomApiResponse<Void>> upsertOverlapRowValues(
            @RequestBody UpdateRowValuesRequest request
    ) {

        service.upsertOverlapRowValues(request);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Overlap row values updated successfully",
                        null
                )
        );
    }

    @Operation(
            summary = "Get Overlap Table Data",
            description = "Fetch overlap table structure, columns, and row values for given orgId and recordType"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Overlap table data fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Overlap table not found")
    })
    @GetMapping("/overlap/table")
    public ResponseEntity<SharkdomApiResponse<ExternalPartnerTableResponse>> getOverlapTableData(
            @RequestParam RecordType recordType
    ) {

        var orgId = Util.getOrgIdFromToken();
        ExternalPartnerTableResponse response =
                service.getOverlapTableData(orgId, recordType);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Overlap table data fetched successfully",
                        response
                )
        );
    }

    @PostMapping("/generate/tables/manual/run")
    public ResponseEntity<List<OverlapRecordFieldEntityTable>> generateTables() {

        List<OverlapRecordFieldEntityTable> tables =
                service.generateTablesForOrganizations();

        return ResponseEntity.ok(tables);
    }

    @PostMapping("/column/default/generate/manual/run")
    public ResponseEntity<List<OverlapRecordFieldEntityTableColumn>> generateDefaultColumns() {

        List<OverlapRecordFieldEntityTableColumn> columns =
                service.createDefaultColumnsForAllTables();

        return ResponseEntity.ok(columns);
    }

    @PostMapping("/migrate/column/data/manual/run")
    public ResponseEntity<String> migrateOverlapRecords() {
        log.info("Migration API started");
        service.migrateOverlapRecordDataToDynamicTable();
        log.info("Migration API completed");
        return ResponseEntity.ok("Overlap record migration completed successfully");
    }



}

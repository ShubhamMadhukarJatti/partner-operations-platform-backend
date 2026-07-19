package com.sharkdom.tablecustomization.controller.externalpartner;

import com.sharkdom.tablecustomization.dto.OrgColumnsResponse;
import com.sharkdom.tablecustomization.dto.externalpartner.*;
import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTableColumn;
import com.sharkdom.tablecustomization.service.externalpartner.ExternalPartnerTableColumnService;
import com.sharkdom.tablecustomization.service.externalpartner.ExternalPartnerTableService;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/external/partner/table")
@RequiredArgsConstructor
@Tag(name = "External Partner Dynamic Table", description = "APIs for managing dynamic table, columns and row values")
public class ExternalPartnerTableColumnController {

    private final ExternalPartnerTableColumnService service;
    private final ExternalPartnerTableService externalPartnerTableService;

    // ============================
    // CREATE COLUMN
    // ============================
    @Operation(
            summary = "Create Column",
            description = "Creates a new visible column in the external partner table"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Column created successfully"),
            @ApiResponse(responseCode = "404", description = "Table not found"),
            @ApiResponse(responseCode = "409", description = "Column already exists")
    })
    @PostMapping("/column")
    public ResponseEntity<SharkdomApiResponse<ExternalPartnerTableColumn>> createColumn(
            @RequestBody CreateColumnExternalPartnerRequest request
    ) {
        ExternalPartnerTableColumn column = service.createColumn(request);
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
            @Parameter(description = "Column ID to soft delete")
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

    // ============================
    // UPSERT ROW VALUES
    // ============================
    @Operation(
            summary = "Upsert Row Values",
            description = "Creates or updates values for a row and its columns"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Row values updated"),
            @ApiResponse(responseCode = "404", description = "Row or Column not found")
    })
    @PutMapping("/row/values")
    public ResponseEntity<SharkdomApiResponse<Void>> upsertRowValues(
            @RequestBody UpdateRowValuesRequest request
    ) {
        service.upsertRowValues(request);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Row values updated successfully",
                        null
                )
        );
    }

    // ============================
    // GET FULL TABLE BY ORG ID
    // ============================
    @Operation(
            summary = "Get Full Table",
            description = "Fetches full table data (only visible columns) with rows and values using organization ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Table fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Table not found")
    })
    @GetMapping("/org")
    public ResponseEntity<SharkdomApiResponse<ExternalPartnerTableResponse>> getFullTable(
    ) {
        var orgId = Util.getOrgIdFromToken();
        ExternalPartnerTableResponse response =
                service.getFullTableByOrgId(orgId);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Table fetched successfully",
                        response
                )
        );
    }

    // ============================
    // GET COLUMNS BY ORG ID
    // ============================
    @Operation(
            summary = "Get External Partner Table Columns",
            description = "Fetches all visible external partner table columns using organization ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Columns fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Table not found")
    })
    @GetMapping("/org/columns")
    public ResponseEntity<SharkdomApiResponse<List<ExternalPartnerTableColumn>>> getColumnsByOrgId() {

        var orgId = Util.getOrgIdFromToken();

        List<ExternalPartnerTableColumn> response =
                externalPartnerTableService.getColumnsByOrgId(orgId);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Columns fetched successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Get All Org Columns",
            description = "Fetches ExternalDocColumns and ExternalPartnerColumns using org ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Columns fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Table not found")
    })
    @GetMapping("/org/all-columns")
    public ResponseEntity<SharkdomApiResponse<OrgColumnsResponse>> getAllColumnsForOrg() {
        OrgColumnsResponse response =
                externalPartnerTableService.getAllColumnsForOrg();
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Columns fetched successfully",
                        response
                )
        );
    }

}

package com.sharkdom.offlinePartner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.dto.SaveAssignmentDto;
import com.sharkdom.entity.externalpartner.ExternalPartnerAssignment;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.model.PersonaStatus;
import com.sharkdom.mypartner.dto.PaginatedResponse;
import com.sharkdom.offlinePartner.dto.CreateExternalPartnerSignDocCommentRequest;
import com.sharkdom.offlinePartner.dto.UpdateExternalPartnerSignDocCommentRequest;
import com.sharkdom.offlinePartner.entity.*;
import com.sharkdom.offlinePartner.model.*;
import com.sharkdom.offlinePartner.repository.OfflinePersonaRepository;
import com.sharkdom.offlinePartner.repository.OfflinePersonaStatusRepository;
import com.sharkdom.offlinePartner.service.DynamicTableService;
import com.sharkdom.offlinePartner.service.ExternalPartnerAssignmentService;
import com.sharkdom.offlinePartner.service.ExternalPartnerSignDocCommentService;
import com.sharkdom.offlinePartner.service.OfflinePartnerService;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.zoho.entity.ZohoSignedDocumentEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Tag(name = "Offline Partner Invite Controller")
@RequestMapping("/v2/offline-partner")
public class OfflinePartnerController {
    private final OfflinePartnerService offlinePartnerService;
    private final OfflinePersonaRepository offlinePersonaRepository;
    private final OfflinePersonaStatusRepository offlinePersonaStatusRepository;
    private final ExternalPartnerAssignmentService externalPartnerAssignmentService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DynamicTableService dynamicTable;
    private final ExternalPartnerSignDocCommentService externalPartnerCommentService;

    public OfflinePartnerController(OfflinePartnerService offlinePartnerService, OfflinePersonaRepository offlinePersonaRepository, OfflinePersonaStatusRepository offlinePersonaStatusRepository, ExternalPartnerAssignmentService externalPartnerAssignmentService, DynamicTableService dynamicTable, ExternalPartnerSignDocCommentService externalPartnerCommentService) {
        this.offlinePartnerService = offlinePartnerService;
        this.offlinePersonaRepository = offlinePersonaRepository;
        this.offlinePersonaStatusRepository = offlinePersonaStatusRepository;
        this.externalPartnerAssignmentService = externalPartnerAssignmentService;
        this.dynamicTable = dynamicTable;
        this.externalPartnerCommentService = externalPartnerCommentService;
    }

    @Operation(summary = "Save Offline Partners")
    @PostMapping("/save")
    public Map<String, String> savePartners(@RequestBody OfflinePartnerSaveRequest offlinePartnerSaveRequest) {
        offlinePartnerService.save(offlinePartnerSaveRequest);
        return Map.of("message", "Partners saved successfully");
    }

    @Operation(summary = "Invite Offline Partners")
    @PostMapping("/invite")
    public List<Map<String, String>> invitePartners(@RequestBody OfflinePartnerInviteRequest offlinePartnerInviteRequest) {
        return offlinePartnerService.invitePartners(offlinePartnerInviteRequest);
    }

    @Operation(summary = "Delete Offline Partner")
    @DeleteMapping
    public void deletePartners(String email) {
        offlinePartnerService.deletePartners(email);
    }

    @Operation(summary = "GET Offline Partner")
    @GetMapping
    public List<OfflinePartnerInvite> getOfflinePartners(@RequestParam(required = false) PartnerInviteStatus status) {
        return offlinePartnerService.getOfflinePartners(status);
    }

    @Operation(summary = "Group Offline Partners")
    @PostMapping("/group")
    public List<OfflinePartnerInvite> groupPartners(@RequestBody GroupPartnerRequest groupPartnerRequest) {
        return offlinePartnerService.groupPartners(groupPartnerRequest);
    }

    @Operation(summary = "Send verification email")
    @PostMapping("/verifyPartnerViaEmail")
    public OfflinePartnerInvite sendVerificationEmail(@RequestBody SendVerificationEmailRequest sendVerificationEmailRequest) {
        return offlinePartnerService.sendVerificationEmail(sendVerificationEmailRequest);
    }

    @Operation(summary = "Get Offline Partners Details By Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OfflinePartnerInviteResponse.class))})})
    @GetMapping("/details/id")
    public OfflinePartnerInviteResponse getPartnerInviteResponse(
            @RequestParam Long id) {
        return offlinePartnerService.getPartnerDetailsById(id);
    }

    @Operation(summary = "Get Offline Partners Details By external partner code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OfflinePartnerInviteResponse.class))})})
    @GetMapping("/details/externalPartnerCode")
    public OfflinePartnerInviteResponse getPartnerInviteResponseByExternalPartner(
            @RequestParam String externalPartnerCode) {
        return offlinePartnerService.getPartnerDetailsByExternalPartnerCode(externalPartnerCode);
    }

    @Operation(summary = "Upload Contract")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OfflinePartnerDocuments.class))})})
    @PostMapping(path = "/uploadContract", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public OfflinePartnerDocuments getPartnerInviteResponse(
            @RequestPart("pdf") MultipartFile pdf,
            @Parameter(
                    required = true,
                    schema = @Schema(implementation = OfflinePartnerDocumentRequest.class)
            ) @RequestPart("offlinePartnerDocumentRequest") String offlinePartnerDocumentRequest) {
        try {
            var request = objectMapper.readValue(offlinePartnerDocumentRequest, OfflinePartnerDocumentRequest.class);
            return offlinePartnerService.uploadFileV1(pdf, request);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    @Operation(summary = "Get Contract")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OfflinePartnerDocuments.class))})})
    @GetMapping("/contract")
    public List<OfflinePartnerDocuments> getContract(@RequestParam String email) {
        return offlinePartnerService.getContract(email);
    }

    @Operation(summary = "Update Partner name and remarks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OfflinePartnerDocuments.class))})})
    @PatchMapping("/partner-details")
    public OfflinePartnerInvite updateDetails(@RequestBody UpdatePartnerDetail request) {
        return offlinePartnerService.updatePartnerDetails(request);
    }

    @Operation(summary = "Save Persona")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(path = "/savePersona")
    public Map<String, String> savePersona(@RequestBody PartnerPersonaRequest personaRequest) {
        var res = offlinePersonaRepository.getAllByOrganizationIdAndPartnerEmail(personaRequest.getOrganizationId(), personaRequest.getPartnerEmail());
        if (res.size() >= 2) {
            throw new SharkdomException(ErrorMessages.SH85);
        }
        var status = offlinePersonaStatusRepository.getByOrganizationIdAndPartnerEmail(personaRequest.getOrganizationId(), personaRequest.getPartnerEmail());
        if (status == null) {
            var entity = OfflinePersonaStatusEntity.builder()
                    .personaStatus(PersonaStatus.INITIATED)
                    .organizationId(personaRequest.getOrganizationId())
                    .partnerEmail(personaRequest.getPartnerEmail()).build();
            offlinePersonaStatusRepository.save(entity);
        }
        offlinePartnerService.savePersona(personaRequest);
        return Map.of("response", "request submitted");
    }

    @Operation(summary = "Persona Controller")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/persona-details")
    public OfflinePersonaResponse getPersonaDetails(
                                                    @RequestParam String partnerEmail,
                                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                                    @RequestParam(value = "size", defaultValue = "20") int size) {
        return offlinePartnerService.getPersonaDetails(partnerEmail, page, size);
    }

    @Operation(summary = "Sign Document using zoho")
    @PostMapping(value = "/sign-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadDocumentForSign(@RequestParam String partnerEmail, @RequestParam MultipartFile file) {
        offlinePartnerService.signOfflinePartnerDocument(partnerEmail, file);
        return Map.of("message", "Document sign request sent successfully");
    }

    @Operation(summary = "GET all documents for organization")
    @GetMapping(value = "/documents")
    public List<ZohoSignedDocumentEntity> getOfflineZohoDocuments() {
        return offlinePartnerService.getOfflineZohoDocuments();
    }

    @PostMapping("/save/assignment")
    public ResponseEntity<ExternalPartnerAssignment> saveAssigment(@RequestBody SaveAssignmentDto saveAssignmentDto) {
        log.info("Received request to save assignment: {}", saveAssignmentDto);
        ExternalPartnerAssignment externalPartnerAssignment = externalPartnerAssignmentService.saveExternalPartnerAssignment(saveAssignmentDto);
        return ResponseEntity.ok(externalPartnerAssignment);
    }

    @GetMapping("/get/partner/assignment/{partnerId}")
    public ResponseEntity<ExternalPartnerAssignment> getMyPartnerAssignment(@PathVariable Long partnerId) {
        log.info("GET /api/my-partner-assignment/{} called", partnerId);
        return ResponseEntity.ok(externalPartnerAssignmentService.getMyPartnerAssignment(partnerId));
    }

    @Operation(summary = "GET Offline Partner (Paginated)")
    @GetMapping("/list")
    public PaginatedResponse<OfflinePartnerInvite> getOfflinePartnerList(
            @RequestParam(required = false) PartnerInviteStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return offlinePartnerService.getOfflinePartnerList(status, page, size, sortBy, sortDir);
    }

    @PostMapping("/document/table/{tableId}/columns")
    public ResponseEntity<SharkdomApiResponse<TableColumn>> createColumn(
            @PathVariable Long tableId,
            @RequestParam String email,
            @RequestBody CreateColumnRequest request
    ) {
        TableColumn column =
                dynamicTable.createColumn(tableId, email, request);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Column created successfully",
                        column
                )
        );
    }

    @PatchMapping("document/table/{tableId}/columns/{columnId}/visibility")
    public ResponseEntity<SharkdomApiResponse<TableColumn>> updateVisibility(
            @PathVariable Long tableId,
            @PathVariable Long columnId,
            @RequestParam String email,
            @RequestBody UpdateColumnVisibilityRequest request
    ) {
        TableColumn updatedColumn =
            dynamicTable.updateColumnVisibility(
                        tableId,
                        columnId,
                        email,
                        request.getVisible()
                );

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Column visibility updated successfully",
                        updatedColumn
                )
        );
    }

    @GetMapping("/document/table/by-email")
    public ResponseEntity<SharkdomApiResponse<DynamicTableResponse>> getTableByEmail(
            @RequestParam String email
    ) {
        DynamicTableResponse data =
                dynamicTable.getTableDataByEmail(email);
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Table data fetched successfully",
                        data
                )
        );
    }

    @PostMapping("/document/table/{tableId}/rows/{rowId}/values")
    public ResponseEntity<SharkdomApiResponse<Void>> saveRowValues(
            @PathVariable Long tableId,
            @PathVariable Long rowId,
            @RequestParam String email,
            @RequestBody SaveRowValuesRequest request
    ) {
        dynamicTable.saveRowValues(tableId, rowId, email, request);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Row values saved successfully",
                        null
                )
        );
    }

    @PatchMapping("/document/table/{tableId}/columns/{columnId}/rename")
    public ResponseEntity<SharkdomApiResponse<TableColumn>> updateColumnName(
            @PathVariable Long tableId,
            @PathVariable Long columnId,
            @RequestParam String email,
            @RequestBody UpdateColumnNameRequest request
    ) {
        TableColumn updatedColumn =
                dynamicTable.updateColumnName(
                        tableId,
                        columnId,
                        email,
                        request.getName()
                );

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Column name updated successfully",
                        updatedColumn
                )
        );
    }

    @DeleteMapping("/document/table/{tableId}/columns/{columnId}")
    public ResponseEntity<SharkdomApiResponse<Void>> deleteColumn(
            @PathVariable Long tableId,
            @PathVariable Long columnId,
            @RequestParam String email
    ) {
        dynamicTable.deleteColumn(tableId, columnId, email);
        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Column deleted successfully",
                        null
                )
        );
    }

    @PatchMapping("/document/table/{tableId}/columns/order")
    public ResponseEntity<SharkdomApiResponse<List<TableColumn>>> updateColumnOrder(
            @PathVariable Long tableId,
            @RequestParam String email,
            @RequestBody UpdateColumnOrderRequest request
    ) {
        List<TableColumn> updatedColumns =
                dynamicTable.updateColumnOrder(
                        tableId,
                        email,
                        request.getColumns()
                );

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Column order updated successfully",
                        updatedColumns
                )
        );
    }

    @GetMapping("/code")
    public SharkdomApiResponse<String> getMessageCodeByEmail(
            @RequestParam String email
    ) {
        log.info("Received request to fetch message code for email: {}", email);
        String messageCode = offlinePartnerService.getOfflinePartnerMessageCodeByEmail(email);
        return new SharkdomApiResponse<>(
                true,
                "Message code fetched successfully",
                messageCode
        );
    }

    @Operation(summary = "Invite Offline Partner")
    @PostMapping("/invites")
    public Map<String, String> invitePartnersToSharkdom(@RequestBody ExternalPartnerInviteRequest offlinePartnerInviteRequest) {
        return offlinePartnerService.inviteExternalPartner(offlinePartnerInviteRequest);
    }

    @Operation(summary = "Create External Partner Comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExternalPartnerSignDocComment.class))
            })
    })
    @PostMapping("/doc-comments")
    public ExternalPartnerSignDocComment createComment(
            @RequestBody CreateExternalPartnerSignDocCommentRequest request
    ) {
        return externalPartnerCommentService.createComment(request);
    }


    @Operation(summary = "Update External Partner Comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExternalPartnerSignDocComment.class))
            })
    })
    @PutMapping("/doc-comments/{commentId}")
    public ExternalPartnerSignDocComment updateComment(
            @PathVariable Long commentId,
            @RequestBody UpdateExternalPartnerSignDocCommentRequest request
    ) {
        return externalPartnerCommentService.updateComment(commentId, request);
    }


    @Operation(summary = "Delete External Partner Comment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok")
    })
    @DeleteMapping("/doc-comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId
    ) {
        externalPartnerCommentService.deleteComment(commentId);

        return ResponseEntity.ok("Comment deleted successfully");
    }


    @Operation(summary = "Get External Partner Comment By Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExternalPartnerSignDocComment.class))
            })
    })
    @GetMapping("/doc-comments/{commentId}")
    public ExternalPartnerSignDocComment getCommentById(
            @PathVariable Long commentId
    ) {
        return externalPartnerCommentService.getCommentById(commentId);
    }


    @Operation(summary = "Get All External Partner Comments By External Partner Code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ExternalPartnerSignDocComment.class))
            })
    })
    @GetMapping("/doc-comments/external-partner/{externalPartnerCode}")
    public List<ExternalPartnerSignDocComment> getAllComments(
            @PathVariable String externalPartnerCode
    ) {
        return externalPartnerCommentService.getAllComments(externalPartnerCode);
    }

}

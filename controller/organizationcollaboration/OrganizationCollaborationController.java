package com.sharkdom.controller.organizationcollaboration;

import com.github.fge.jsonpatch.JsonPatch;
import com.sharkdom.constants.Flag;
import com.sharkdom.constants.LinkerType;
import com.sharkdom.dto.OrganizationCollaborationGroupingDataCountDTO;
import com.sharkdom.dto.OrganizationPartnerCategoryResponse;
import com.sharkdom.dto.OrganizationPartnerResponse;
import com.sharkdom.dto.SaveAssignmentDto;
import com.sharkdom.entity.mou.MouHistory;
import com.sharkdom.entity.mypartner.MyPartnerAssignment;
import com.sharkdom.entity.organizationcollaboration.*;
import com.sharkdom.entity.organizationcollaboration.dto.MessageResponse;
import com.sharkdom.entity.organizationcollaboration.dto.PartnerSpaceRequest;
import com.sharkdom.model.mou.MouSignRequest;
import com.sharkdom.model.organizatiocollaboration.*;
import com.sharkdom.service.organizationcollaboration.OrganizationCollaborationGroupService;
import com.sharkdom.service.organizationcollaboration.OrganizationCollaborationService;
import com.sharkdom.zoho.entity.ZohoSignedDocumentEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController

@RequestMapping("/organizationCollaboration")
@Slf4j
public class OrganizationCollaborationController {

    private final OrganizationCollaborationService organizationCollaborationService;
    private final OrganizationCollaborationGroupService organizationCollaborationGroupService;

    public OrganizationCollaborationController(OrganizationCollaborationService organizationCollaborationService, OrganizationCollaborationGroupService organizationCollaborationGroupService) {
        this.organizationCollaborationService = organizationCollaborationService;
        this.organizationCollaborationGroupService = organizationCollaborationGroupService;
    }


    @Deprecated
    @Operation(summary = "Update an existing organizationCollaboration by sending organizationCollaboration json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the OrganizationCollaboration and updated it.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationCollaboration.class))}),
            @ApiResponse(responseCode = "404", description = "OrganizationCollaboration not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PutMapping("")
    public ResponseEntity<OrganizationCollaboration> update(@RequestBody OrganizationCollaboration updated) throws Exception {
        return ResponseEntity.ok(organizationCollaborationService.update(updated));
    }

    @Operation(summary = "AcceptProposal by sending receiverOrganizationId,senderOrganizationId and acceptorUserId ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the OrganizationCollaboration and updated it.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationCollaboration.class))}),
            @ApiResponse(responseCode = "404", description = "OrganizationCollaboration not found with given  receiverOrganizationId,senderOrganizationId and acceptorUserId id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PutMapping("/acceptCollabRequest")
    public ResponseEntity<HttpStatus> acceptCollabRe(@RequestParam(name = "receiverOrganizationId") long receiverOrganizationId,
                                                     @RequestParam(name = "senderOrganizationId") long senderOrganizationId,
                                                     @RequestParam(name = "acceptorUserId") String acceptorUserId) throws Exception {
        return ResponseEntity.ok(organizationCollaborationService.acceptCollabRequest(senderOrganizationId, receiverOrganizationId, acceptorUserId));
    }

    @Operation(summary = "Update/Create an organizationCollaboration by sending organizationCollaboration json | no need to pass organization collaboration id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the OrganizationCollaboration and updated it.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationCollaboration.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("")
    public ResponseEntity<OrganizationCollaboration> create(@RequestBody OrganizationCollaboration create) throws Exception {
        return ResponseEntity.ok(organizationCollaborationService.createOrUpdateCollaboration(create));
    }

    @Operation(summary = "Get all collaboration requests send by an organization by it's organizationId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the OrganizationCollaboration.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationCollaboration.class))}),
            @ApiResponse(responseCode = "404", description = "OrganizationCollaboration not found with given senderOrganizationId", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/senderOrganizationId")
    public ResponseEntity<Page<OrganizationCollaboration>> findBySenderOrganizationId(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(organizationCollaborationService.findBySenderOrganizationId(page, size));
    }

    @Operation(summary = "Get all collaboration requests received by an organization by it's organizationId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the OrganizationCollaboration", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationCollaboration.class))}),
            @ApiResponse(responseCode = "404", description = "OrganizationCollaboration not found with given receiverOrganizationId", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/receiverOrganizationId")
    public ResponseEntity<Page<OrganizationCollaboration>> findByReceiverOrganizationId(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                                        @RequestParam(value = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(organizationCollaborationService.findByReceiverOrganizationId(page, size));
    }

    @Operation(summary = "Use json patch to partially update an OrganizationCollaboration, for more details refer https://www.baeldung.com/spring-rest-json-patch ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OrganizationCollaboration updated successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationCollaboration.class))}),
            @ApiResponse(responseCode = "404", description = "OrganizationCollaboration not found with given code", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PatchMapping(path = "/id", consumes = "application/json-patch+json")
    public ResponseEntity<OrganizationCollaboration> patchOrganizationCollaborationById(@RequestParam(name = "id") long id,
                                                                                        @Parameter(description = "MyDto") @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                                                                                                @ExampleObject(name = "Sending one replace and one add operation", value = "[\r\n"
                                                                                                        + "    {\"op\":\"replace\",\"path\":\"/senderOrganizationId\",\"value\":\"986\"},\r\n"
                                                                                                        + "    {\"op\":\"add\",\"path\":\"/senderUserId\",\"value\":\"xyz\"}\r\n" + "]")})) @RequestBody JsonPatch patch)
            throws Exception {
        log.info("Received patch request with id: " + id + " and json: " + patch);
        return ResponseEntity.ok(organizationCollaborationService.patchByOrganizationCollaborationId(id, patch));
    }


    @Operation(summary = "Get OrganizationCollaboration Details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OrganizationCollaboration retrieved  successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationCollaboration.class))}),
            @ApiResponse(responseCode = "404", description = "OrganizationCollaboration not found with given code", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/id")
    public ResponseEntity<OrganizationCollaboration> getOrganizationCollaborationById(@RequestParam(name = "id") Long id) {
        return ResponseEntity.ok(organizationCollaborationService.getOrganizationCollaborationById(id));
    }

    @Operation(summary = "Get an OrganizationCollaboration by sending senderOrganizationId and receiverOrganizationId ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the OrganizationCollaboration.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationCollaboration.class))}),
            @ApiResponse(responseCode = "404", description = "OrganizationCollaboration not found with given senderOrganizationId and receiverOrganizationId", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/senderAndReceiverIds")
    public ResponseEntity<OrganizationCollaboration> findBySenderOrganizationIdAndReceiverOrganizationId(@RequestParam(name = "senderOrganizationId") long senderOrganizationId,
                                                                                                         @RequestParam(name = "receiverOrganizationId") long receiverOrganizationId) throws Exception {
        return ResponseEntity.ok(organizationCollaborationService.findBySenderOrganizationIdAndReceiverId(senderOrganizationId, receiverOrganizationId));
    }

    @Operation(summary = "Get OrganizationCollaboration between two organizations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the OrganizationCollaboration.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationCollaboration.class))}),
            @ApiResponse(responseCode = "404", description = "OrganizationCollaboration not found with given senderOrganizationId and receiverOrganizationId", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/betweenTwoOrganizationsByTheirIds")
    public ResponseEntity<OrganizationCollaboration> getCollabBetweenTwoOrganizations(@RequestParam(name = "firstOrganizationId") long firstOrganizationId,
                                                                                      @RequestParam(name = "secondOrganizationId") long secondOrganizationId) throws Exception {
        return ResponseEntity.ok(organizationCollaborationService.findCollabBetweenTwoOrganizations(firstOrganizationId, secondOrganizationId));
    }

    @Operation(summary = "Messages between two organizations",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = OrganizationMessages.class))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages Saved"),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/messages")
    public ResponseEntity<OrganizationMessagesResponse> sendMessage(@RequestBody SendMessageRequest organizationMessages) {
        return ResponseEntity.ok(organizationCollaborationService.sendMessage(organizationMessages));
    }

    @Operation(summary = "Get partner-space messages between two organizations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/messages/{chatRoomId}")
    public ResponseEntity<MessageResponse> getMessages(@PathVariable Long chatRoomId, @RequestParam(required = false) ChannelFlag channelType, @RequestParam(required = false) Flag flag, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        MessageResponse response = organizationCollaborationService.getMessageResponse(chatRoomId, channelType, flag, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all collaboration by an organizationId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the OrganizationCollaboration.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationCollaboration.class))}),
            @ApiResponse(responseCode = "404", description = "OrganizationCollaboration not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping
    public ResponseEntity<Page<OrganizationCollaboration>> findByOrganizationId(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(organizationCollaborationService.findByOrganizationId(page, size));
    }

    @Operation(summary = "Update isViewed flag to true")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated isViewed flag to true", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationCollaboration.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/isviewed")
    public ResponseEntity<OrganizationCollaboration> updateIsViewed(@RequestParam Long orgCollaborationId) {
        return ResponseEntity.ok(organizationCollaborationService.updateIsViewed(orgCollaborationId));
    }

    @Operation(summary = "Update isEmailOpened flag to true")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated isViewed flag to true", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationCollaboration.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/isEmailOpened")
    public ResponseEntity<OrganizationCollaboration> updateIsEmailOpened(@RequestParam Long orgCollaborationId) {
        return ResponseEntity.ok(organizationCollaborationService.updateIsEmailOpened(orgCollaborationId));
    }

    @Operation(summary = "Update isEmailClicked flag to true")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated isViewed flag to true", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationCollaboration.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/isEmailClicked")
    public ResponseEntity<OrganizationCollaboration> updateIsEmailClicked(@RequestParam Long orgCollaborationId) {
        return ResponseEntity.ok(organizationCollaborationService.updateIsEmailClicked(orgCollaborationId));
    }

    @Operation(summary = "GET Timeline")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated isViewed flag to true", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TimelineEntity.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/timeline")
    public ResponseEntity<List<TimelineEntity>> getTimeline() {
        return ResponseEntity.ok(organizationCollaborationService.getTimeline());
    }

    @Operation(summary = "Update isViewed flag to true")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated isViewed flag to true", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationCollaboration.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/timeline")
    public ResponseEntity<OrganizationCollaboration> saveTimeline(@RequestParam Long orgCollaborationId, String template) {
        return ResponseEntity.ok(organizationCollaborationService.saveTimeline(orgCollaborationId, template));
    }


    @Operation(summary = "Get Partner details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get Partner details", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrgCollaborationWithCredits.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/partner-details")
    public ResponseEntity<OrgCollaborationWithCredits> getPartner(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                  @RequestParam(value = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(organizationCollaborationService.getPartnerDashboard(page, size));
    }

    @Operation(summary = "Upload Contract ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OfflineContract.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(value = "/upload-contract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OfflineContract> uploadContract(@RequestParam String org1Email, @RequestParam String org2Email, @RequestPart("document") MultipartFile document) {
        return ResponseEntity.ok(organizationCollaborationService.saveOfflineContract(org1Email, org2Email, document));
    }

    @Operation(summary = "GET Contract ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OfflineContract.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(value = "/contract")
    public ResponseEntity<List<OfflineContract>> getContract(@RequestParam String org1Email, @RequestParam String org2Email) {
        return ResponseEntity.ok(organizationCollaborationService.getOfflineContract(org1Email, org2Email));
    }

    @Operation(summary = "GET MOU Pdf ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = MouHistory.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(value = "/mou-pdf")
    public ResponseEntity<Optional<MouHistory>> getMouPdf(@RequestParam Long organizationCollaborationId, @RequestParam Long organizationId) {
        return ResponseEntity.ok(organizationCollaborationService.getMouHistory(organizationCollaborationId, organizationId));
    }

    @Operation(summary = "GET Envelope details ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = EnvelopeEntity.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(value = "/envelope")
    public ResponseEntity<Optional<EnvelopeEntity>> getEnvelopeDetails(@RequestParam String envelopeId) {
        return ResponseEntity.ok(organizationCollaborationService.getEnvelopeById(envelopeId));
    }

    @Operation(summary = "GET all pending mous for an organization ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = MouHistory.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(value = "/pending-mous")
    public ResponseEntity<List<MouHistory>> getAllPendingMou() {
        return ResponseEntity.ok(organizationCollaborationService.getAllPendingMou());
    }

    @Operation(summary = "Sign MOU")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = MouHistory.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(value = "/sign-mou", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Map<String, String>> signMou(
            @Parameter(description = "MOU file to upload") @RequestPart("mou") MultipartFile mou,
            @RequestParam Long organizationCollaborationId, @RequestParam Long organizationId, @RequestParam String envelopeId) {
        var mouSignRequest = new MouSignRequest(organizationCollaborationId, organizationId, envelopeId);
        organizationCollaborationService.signMou(mouSignRequest, mou);
        return ResponseEntity.ok(Map.of("message", "MOU signed successfully"));
    }

    @Operation(summary = "Create Collaboration Category")
    @PostMapping(value = "/collaboration-category", consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<OrganizationCollaborationCategoryEntity> createCollaborationCategory(
            @RequestBody CollaborationCategoryRequest collaborationCategoryRequest) {
        return organizationCollaborationService.createCollaborationCategory(collaborationCategoryRequest);
    }

    @Operation(summary = "GET all Collaborations")
    @GetMapping(value = "/my-partners")
    public Page<PartnerDashboardResponse> getAllCollaborations(
            @RequestParam(required = false, defaultValue = "ALL") CollaborationStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return organizationCollaborationService.getAllCollaborations(status, page, size);
    }

    @Operation(summary = "Create a new partner space room")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "create a new partner-space",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PartnerSpaceRoom.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/partner-space")
    public ResponseEntity<PartnerSpaceRoom> createPartnerSpace(@RequestBody PartnerSpaceRequest request) {
        return ResponseEntity.ok(organizationCollaborationService.createPartnerSpaceRoom(request));
    }

    @Operation(summary = "Get partner spaces by organization ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of partner spaces",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PartnerSpaceRoom.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/partner-space/organization")
    public ResponseEntity<List<PartnerSpaceRoom>> getPartnerSpacesByOrganizationId() {
        List<PartnerSpaceRoom> rooms = organizationCollaborationService.getPartnerSpaceByOrganizationId();
        return ResponseEntity.ok(rooms);
    }

    @Operation(summary = "Edit proposal details")
    @PostMapping("/{collaborationId}/edit")
    public ResponseEntity<Map<String, String>> updateProposalDetails(@PathVariable Long collaborationId, @RequestBody ProposalEditRequest request) {
        organizationCollaborationService.updateProposalDetails(collaborationId, request);
        return ResponseEntity.ok(Map.of("message", "proposal edited successfully"));
    }

    @Operation(summary = "Update proposal history status")
    @PostMapping("/{historyId}/update/status")
    public ResponseEntity<Map<String, String>> updateStatus(@PathVariable Long historyId, @RequestParam EditHistoryStatus status) {
        organizationCollaborationService.updateProposalEditStatus(historyId, status);
        return ResponseEntity.ok(Map.of("message", "proposal status updated successfully"));
    }

    @Operation(summary = "Get proposal edit history")
    @GetMapping("/{collaborationId}/history")
    public ResponseEntity<List<List<ProposalEditHistoryEntity>>> getHistoryDetails(@PathVariable Long collaborationId) {
        return organizationCollaborationService.getProposalEditHistory(collaborationId);
    }

    @Operation(summary = "check partner space exists else create a new one")
    @GetMapping("/{collaborationId}/partner-space")
    public ResponseEntity<Map<String, Boolean>> checkPartnerSpaceExists(@PathVariable Long collaborationId) {
        organizationCollaborationService.checkPartnerSpaceExists(collaborationId);
        return ResponseEntity.ok(Map.of("spaceCreated", true));
    }

    @Operation(summary = "get benefits details ")
    @GetMapping("/benefits")
    public ResponseEntity<BenefitsResponse> getBenefitsDetails(@RequestParam Long benefitId, @RequestParam LinkerType linkerType) {
        return ResponseEntity.ok(organizationCollaborationService.getBenefitsDetails(benefitId, linkerType));
    }

    @Operation(summary = "GET all documents for organization collaboration")
    @GetMapping(value = "/documents")
    public List<ZohoSignedDocumentEntity> getOfflineZohoDocuments(@RequestParam Long organizationIdCollaborationId) {
        return organizationCollaborationService.getZohoDocuments(organizationIdCollaborationId);
    }

    @PostMapping("/save/assignment")
    public ResponseEntity<MyPartnerAssignment> saveAssigment(@RequestBody SaveAssignmentDto saveAssignmentDto) {
        log.info("OrganizationCollaborationController >> saveAssignment");
        MyPartnerAssignment myPartnerAssignment = organizationCollaborationService.saveMyPartnerAssignment(saveAssignmentDto);
        return ResponseEntity.ok(myPartnerAssignment);
    }

    @Operation(
            summary = "Get all organization partners grouped by category",
            description = "Fetches all organization partners grouped under categories such as RELIABLE_PARTNER, STEADY_PARTNER, LOW_IMPACT_PARTNER, and INACTIVE_PARTNER."
    )
    @GetMapping("/segments/data")
    public ResponseEntity<List<OrganizationPartnerCategoryResponse>> getPartnersByAllCategories() {
        log.info("Received request to fetch partners grouped by all categories.");

        try {
            List<OrganizationPartnerCategoryResponse> partners =
                    organizationCollaborationGroupService.getPartnersByAllCategories();

            if (partners == null || partners.isEmpty()) {
                log.warn("No partners found for any category.");
                return ResponseEntity.ok(Collections.emptyList());
            }

            log.info("Successfully fetched {} partner category groups.", partners.size());
            return ResponseEntity.ok(partners);

        } catch (Exception e) {
            log.error("Error occurred while fetching partners grouped by categories.", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/get/partner/assignment/{partnerOrgId}")
    public MyPartnerAssignment getMyPartnerAssignment(@PathVariable Long partnerOrgId) {
        log.info("GET /api/my-partner-assignment/{} called", partnerOrgId);
        return organizationCollaborationService.getMyPartnerAssignment(partnerOrgId);
    }

    @GetMapping("/segments/data/count")
    public ResponseEntity<OrganizationCollaborationGroupingDataCountDTO> getOrganizationCollaborationGroupingDataCount() {
        log.info("Received request to fetch organization collaboration grouping data count.");
        try {
            OrganizationCollaborationGroupingDataCountDTO response =
                    organizationCollaborationGroupService.getOrganizationCollaborationGroupingDataCount();
            log.info("Successfully fetched organization collaboration grouping data count.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error while fetching organization collaboration grouping data count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}

package com.sharkdom.controller.organization;

import com.github.fge.jsonpatch.JsonPatch;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.SwaggerConstants;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.constants.organization.OrganizationStatus;
import com.sharkdom.dto.*;
import com.sharkdom.entity.organization.*;
import com.sharkdom.entity.organizationcollaboration.OrganizationMessages;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.integration.model.IntegrationSaveRequest;
import com.sharkdom.model.meetings.CreateMeetingModel;
import com.sharkdom.model.organization.*;
import com.sharkdom.model.organization.GettingStartedResponse;
import com.sharkdom.offlinePartner.repository.OfflinePartnerInviteRepository;
import com.sharkdom.profilesection.repository.OrganizationCertificationRepository;
import com.sharkdom.profilesection.service.OrganizationCertificationService;
import com.sharkdom.repository.catalogue.PartnerTierRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organization.ShortlistOrganizationRepository;
import com.sharkdom.service.credits.CreditService;
import com.sharkdom.service.organization.DweepAISearch;
import com.sharkdom.service.organization.OrganizationCurrencyService;
import com.sharkdom.service.organization.OrganizationService;
import com.sharkdom.service.organization.ShortlistingService;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import com.sharkdom.zoho.dto.ZohoWebhookDetailsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/organization")
@Validated
@Slf4j
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ShortlistingService shortlistingService;

    @Autowired
    private DweepAISearch dweepAISearch;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationCurrencyService organizationCurrencyService;

    @Autowired
    private OfflinePartnerInviteRepository offlinePartnerInviteRepository;

    @Autowired
    private PartnerTierRepository partnerTierRepository;

    @Autowired
    private OrganizationCertificationRepository organizationCertificationRepository;

    @Autowired
    private OrganizationCertificationService organizationCertificationService;

    @Operation(summary = "Update an existing organization by sending organization json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the organization and updated it.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Organization.class))}),
            @ApiResponse(responseCode = "404", description = "Organization not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PutMapping("")
    public ResponseEntity<Organization> update(@RequestBody Organization updated) throws Exception {
        return ResponseEntity.ok(organizationService.update(updated));
    }

    @Operation(summary = "Create new organization by sending user json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New organization created successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Organization.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("")
    public ResponseEntity<Organization> create(@Valid @RequestBody Organization created) {
        return ResponseEntity.ok(organizationService.create(created));
    }

    @Operation(summary = "Get an organization by its id, returns Optional<Organization>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the organization.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Organization.class))}),
            @ApiResponse(responseCode = "404", description = "Organization not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/id")
    public ResponseEntity<Optional<Organization>> findById(@RequestParam(name = "id") long id) throws Exception {
        return ResponseEntity.ok(organizationService.findById(id));
    }

    @Operation(summary = "Get an organization by its code, returns Optional<Organization>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the organization.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Organization.class))}),
            @ApiResponse(responseCode = "404", description = "Organization not found with given code", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/code")
    public ResponseEntity<Optional<Organization>> findByCode(@RequestParam(name = "code") String code) throws Exception {
        return ResponseEntity.ok(organizationService.findByCode(code));
    }

    @Operation(summary = "Check if an organization code is available or not.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service responded OK", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/isCodeAvailable")
    public ResponseEntity<Map<String, Boolean>> isCodeAvailable(@RequestParam(name = "code") String code) throws Exception {
        return ResponseEntity.ok(Collections.singletonMap("isAvailable", organizationService.isCodeAvailable(code)));
    }

    @Operation(summary = "Check if an organization name is available or not.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service responded OK", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/isNameAvailable")
    public ResponseEntity<Map<String, Boolean>> isNameAvailable(@RequestParam(name = "name") String name) throws Exception {
        return ResponseEntity.ok(Collections.singletonMap("isAvailable", organizationService.isNameAvailable(name)));
    }

    @Operation(summary = "Search for an organization by it's partial name, case insensitive.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service responded OK", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Organization.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/searchByPartialNameppp")
    public ResponseEntity<Page<Organization>> searchByPartialName(@RequestParam(name = "partialName") String partialName,
                                                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                                                  @RequestParam(value = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(organizationService.searchByPartialName(partialName, page, size));
    }

    @Operation(summary = "Search for an organization by it's partial name, case insensitive.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service responded OK", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Organization.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/searchByPartialName/v1")
    public ResponseEntity<Page<Organization>> searchByPartialNameV1(@RequestParam(name = "partialName") String partialName,
                                                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                                                  @RequestParam(value = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(organizationService.searchByEmailAndName(partialName, page, size));
    }

    @Operation(summary = "Use json patch to partially update an organization, for more details refer https://www.baeldung.com/spring-rest-json-patch ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization updated successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Organization.class))}),
            @ApiResponse(responseCode = "404", description = "Organization not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PatchMapping(path = "", consumes = "application/json-patch+json")
    public ResponseEntity<Organization> patchOrganizationById(@RequestParam(name = "id") long id,
                                                              @Parameter(description = "MyDto") @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                                                                      @ExampleObject(name = "Sending one replace and one add operation", value = "[\r\n"
                                                                              + "    {\"op\":\"replace\",\"path\":\"/mobile\",\"value\":\"+91 8744956986\"},\r\n"
                                                                              + "    {\"op\":\"add\",\"path\":\"/name\",\"value\":\"Mike\"}\r\n" + "]"),
                                                                      @ExampleObject(name = "Sending remove operation to remove briefDescription", value = "[{\"op\":\"remove\",\"path\":\"/briefDescription\"}]")})) @RequestBody JsonPatch patch)
            throws Exception {
        log.info("Received patch request with organization id: " + id + " and json: " + patch);
        return ResponseEntity.ok(organizationService.patchById(id, patch));
    }

    @Operation(summary = "Search for an organization by city,state,inceptionYear,stage,sector")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service responded OK", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Organization.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/search")
    public ResponseEntity<Page<OrganizationResponse>> searchOrganization(@RequestParam(name = "city", defaultValue = "") String city,
                                                                         @RequestParam(value = "state", defaultValue = "") String state,
                                                                         @RequestParam(value = "inceptionYearFrom", defaultValue = "0") int inceptionYearFrom,
                                                                         @RequestParam(value = "stagesCommaSeparated", required = false) String stage,
                                                                         @RequestParam(value = "sectorsCommaSeparated", required = false) String sector,
                                                                         @RequestParam(value = "partnershipTypesCommaSeparated", required = false) String partnershipTypes,
                                                                         @RequestParam(value = "includeUnverified", defaultValue = "1") boolean includeUnverified,
                                                                         @RequestParam(value = "queryingOrganizationId", defaultValue = "0") long queryingOrganizationId,
                                                                         @RequestParam(value = "subSectorsCommaSeparated", required = false) String subSector,
                                                                         @RequestParam(value = "size", defaultValue = "20") int size,
                                                                         @RequestParam(value = "page", defaultValue = "0") int page) {
        return ResponseEntity.ok(organizationService.searchOrganization(city, state, stage, inceptionYearFrom, sector, includeUnverified, queryingOrganizationId, partnershipTypes, subSector, size, page));
    }

    @Operation(summary = "Search for an organization by city,state,inceptionYear,stage,sector,companyType")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service responded OK", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Organization.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/searchPartial/v2")
    public ResponseEntity<Page<OrganizationResponse>> searchPartialOrganization(
            @RequestParam(name = "partialName", defaultValue = "") String partialName,
            @RequestParam(name = "city", defaultValue = "") String city,
            @RequestParam(value = "state", defaultValue = "") String state,
            @RequestParam(value = "inceptionYearFrom", defaultValue = "0") int inceptionYearFrom,
            @RequestParam(value = "stagesCommaSeparated", required = false) String stage,
            @RequestParam(value = "sectorsCommaSeparated", required = false) String sector,
            @RequestParam(value = "partnershipTypesCommaSeparated", required = false) String partnershipTypes,
            @RequestParam(value = "includeUnverified", defaultValue = "1") boolean includeUnverified,
            @RequestParam(value = "queryingOrganizationId", defaultValue = "0") long queryingOrganizationId,
            @RequestParam(value = "subSectorsCommaSeparated", required = false) String subSector,
            @RequestParam(value = "companyTypeCommaSeparated", required = false) String companyType,
            @RequestParam(defaultValue = "false") boolean exactMatch,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "page", defaultValue = "0") int page) {
        return ResponseEntity.ok(organizationService.searchPartialOrganization(city, state, stage, inceptionYearFrom, sector, includeUnverified, queryingOrganizationId, partnershipTypes, size, page, partialName, subSector, companyType, exactMatch));
    }

    @Operation(summary = "Search for an organization by city,state,inceptionYear,stage,sector,companyType")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service responded OK", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Organization.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/searchPartial/v1")
    public ResponseEntity<Page<OrganizationResponse>> searchPartialOrganizations(
            @RequestParam(value = "filtersCommaSeparated", required = false) String filters,
            @RequestParam(value = "sectorsCommaSeparated", required = false) String sector,
            @RequestParam(value = "partnershipTypesCommaSeparated", required = false) String partnershipTypes,
            @RequestParam(value = "subSectorsCommaSeparated", required = false) String subSector,
            @RequestParam(value = "size", defaultValue = "3") int size,
            @RequestParam(value = "page", defaultValue = "0") int page) {
        return ResponseEntity.ok(organizationService.searchPartialOrganizations(filters, sector,partnershipTypes, size, page, subSector));
    }


    @Operation(summary = "Search for an organization by sectors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service responded OK", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationSearchResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/v2/search-partial")
    public ResponseEntity<List<OrganizationSearchResponse>> searchPartialOrganizationV2(
            @RequestParam(value = "subSectorsCommaSeparated", required = false) String subSector,
            @RequestParam(value = "partnershipTypesCommaSeparated", required = false) String partnershipTypes,
            @RequestParam(value = "sectorsCommaSeparated", required = false) String sector
            ){
        return ResponseEntity.ok(organizationService.searchPartialOrganizationV2(subSector, partnershipTypes, sector));
    }


    @Operation(summary = "Verify organization by PAN")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service responded OK", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = VerificationResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/verify")
    public VerificationResponse verifyOrganization(@RequestBody VerificationRequest verificationRequest) {
        return organizationService.verifyOrganization(verificationRequest);
    }

    @Operation(summary = "Upload logo by organizationId")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service responded OK", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = Organization.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(value = "/upload/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Organization uploadOrganizationLogo(@RequestParam MultipartFile logo) {
        return organizationService.uploadLogo(logo);
    }

    @Operation(summary = "Save integration details")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service responded OK", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = IntegrationDetails.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(value = "/integration")
    public IntegrationDetails saveIntegrationDetails(@Valid @RequestBody IntegrationSaveRequest integrationDetails) {
        return organizationService.saveIntegrationDetailsV1(integrationDetails);
    }

    @Operation(summary = "Update integration details", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = CreateMeetingModel.class),
            examples = @ExampleObject(value = SwaggerConstants.PATCH_INTEGRATION))))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service responded OK", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = IntegrationDetails.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PatchMapping(value = "/integration")
    public IntegrationDetails updateIntegrationDetails(@RequestBody IntegrationDetails integrationDetails) {
        return organizationService.updateIntegration(integrationDetails);
    }

    @Operation(summary = "Get integration details by organizationId")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service responded OK", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = IntegrationDetails.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(value = "/integration")
    public List<IntegrationDetails> getIntegrationDetails() {
        return organizationService.getIntegrationDetails();
    }

    @Operation(summary = "Upload document by organizationId")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service responded OK", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = OrgDocumentsEntity.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(value = "/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public OrgDocumentsEntity uploadOrganizationDocument(@RequestParam String documentType, @RequestParam MultipartFile document) {
        if (!"application/pdf".equalsIgnoreCase(document.getContentType())) {
            throw new SharkdomException(ErrorMessages.SH86);
        }
        return organizationService.uploadDocument(documentType, document);
    }

    @Operation(summary = "GET documents by organizationId")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service responded OK", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = OrgDocumentsEntity.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(value = "/document")
    public List<OrgDocumentsEntity> getOrganizationDocument() {
        return organizationService.getOrganizationDocuments();
    }

    @Operation(summary = "Use json patch to partially update document status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document status updated successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrgDocumentsEntity.class))}),
            @ApiResponse(responseCode = "404", description = "Document not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PatchMapping(path = "/document", consumes = "application/json-patch+json")
    public Mono<OrgDocumentsEntity> patchDocumentById(@RequestParam(name = "id") long id,
                                                      @Parameter(description = "MyDto") @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                                                              @ExampleObject(name = "Sending one replace and one add operation", value = "[\r\n"
                                                                      + "    {\"op\":\"replace\",\"path\":\"/status\",\"value\":\"REJECTED\"},\r\n"
                                                                      + "    {\"op\":\"replace\",\"path\":\"/remarks\",\"value\":\"PDF is not clear\"}\r\n" + "]")})) @RequestBody JsonPatch patch)
            throws Exception {
        log.info("Received patch request with document id: " + id + " and json: " + patch);
        return organizationService.patchDocument(id, patch);
    }

    @Operation(summary = "Get organizations created between from and to")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Organization.class))})})
    @GetMapping("/data")
    public Mono<List<OrgData>> getReferralData(@Schema(defaultValue = "2024-05-07") @RequestParam(required = false) String from,
                                               @Schema(defaultValue = "2024-05-12") @RequestParam(required = false) String to) {
        return organizationService.findAllFromTo(from, to);
    }

    @Operation(summary = "Get Message by organizationId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationMessages.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/messages/{organizationId}")
    public List<Map<String, Object>> getMessage() {
        return organizationService.getChatRoomSummaryForOrg();
    }

    @Operation(summary = "Get Message by organizationId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationMessages.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PatchMapping("/messages/{messageId}")
    public void markMessageAsRead(@PathVariable Long messageId) {
        organizationService.markMessageAsRead(messageId);
    }

    @Operation(summary = "Save pilot program")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service responded OK", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = PilotProgram.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(value = "/pilot-program/{organizationId}")
    public PilotProgram savePilotProgram(@PathVariable Long organizationId, @RequestBody PilotProgram pilotProgram) {
        return organizationService.savePilotProgram(organizationId, pilotProgram);
    }

    @Operation(summary = "Get all pilot program by organizationId")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service responded OK", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = PilotProgram.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(value = "/pilot-program/{organizationId}")
    public List<PilotProgram> savePilotProgram() {
        return organizationService.findAllPilotProgram();
    }

    @Operation(summary = "Bookmark Organization")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service responded OK", content = {
            @Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping(value = "/bookmark")
    public Map<String, String> bookmarkOrganization(@RequestParam Long organizationId, @RequestParam Long partnerOrganizationId) {
        organizationService.bookmarkOrganization(organizationId, partnerOrganizationId);
        return Map.of("response", "organization bookmarked successfully");
    }

    @Operation(summary = "Bookmark Organization Data")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service responded OK", content = {
            @Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(value = "/bookmark")
    public Page<OrganizationSearchResponse> getBookmarkOrganization(@RequestParam Long organizationId, @RequestParam(value = "page", defaultValue = "0") int page,
                                                                    @RequestParam(value = "size", defaultValue = "20") int size) {
        return organizationService.getBookmarkOrganizationDetails(organizationId, page, size);
    }

    @Operation(summary = "Remove Bookmark Organization")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service responded OK", content = {
            @Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @DeleteMapping(value = "/bookmark")
    public Map<String, String> removeBookmarkOrganization(@RequestParam Long organizationId, @RequestParam Long partnerOrganizationId) {
        organizationService.removeBookmarkOrganization(organizationId, partnerOrganizationId);
        return Map.of("response", "organization removed successfully");
    }

    @Operation(summary = "Getting Started")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Service responded OK", content = {
            @Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(value = "/getting-started")
    public GettingStartedResponse gettingStarted() {
        return organizationService.gettingStarted();
    }

    @Operation(summary = "Invite Partner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json")})})
    @PostMapping("/addPartner")
    public Map<String, String> invitePartner(@RequestParam String email,
                                             @RequestParam Long organizationId,
                                             @RequestParam String message,
                                             @RequestParam String name) {
        return organizationService.invitePartner(email, organizationId, message, name);
    }

    @Operation(summary = "Bulk Invite Partner")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json")})})
    @PostMapping("/addPartners")
    public List<Map<String, String>> bulknvitePartner(@RequestBody InvitePartnerRequest invitePartnerRequest) {
        return organizationService.bulkInvitePartner(invitePartnerRequest);
    }

    @Operation(summary = "Get Partners")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PartnerInvite.class))})})
    @GetMapping("/partner")
    public List<PartnerInvite> getAllInvites(
            @RequestParam Long organizationId) {
        return organizationService.getPartnerInvite(organizationId);
    }

    @Operation(summary = "Use json patch to partially update proposalSent status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document status updated successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrgDocumentsEntity.class))}),
            @ApiResponse(responseCode = "404", description = "Document not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PatchMapping(path = "/getting-started", consumes = "application/json-patch+json")
    public GettingStartedEntity patchGettingStarted(@RequestParam(name = "organizationId") long organizationId,
                                                    @Parameter(description = "MyDto") @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                                                            @ExampleObject(name = "Sending one replace and one add operation", value = """
                                                                    [\r
                                                                        {"op":"replace","path":"/proposalSetupStatus","value":"true"}
                                                                        
                                                                    ]""")})) @RequestBody JsonPatch patch)
            throws Exception {

        return organizationService.patchGettingStarted(organizationId, patch);
    }

    @Operation(summary = "Use json patch to partially update invite status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document status updated successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrgDocumentsEntity.class))}),
            @ApiResponse(responseCode = "404", description = "Document not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PatchMapping(path = "/partner", consumes = "application/json-patch+json")
    public PartnerInvite patchPartnerInvite(@RequestParam(name = "email") String email,
                                            @Parameter(description = "MyDto") @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                                                    @ExampleObject(name = "Sending one replace and one add operation", value = """
                                                            [\r
                                                                {"op":"replace","path":"/status","value":"ACTIVE"}
                                                                
                                                            ]""")})) @RequestBody JsonPatch patch)
            throws Exception {

        return organizationService.patchPartnerInvite(email, patch);
    }

    @Operation(summary = "Save Organization Availability")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationAvailability.class))})})
    @PostMapping("/availability")
    public OrganizationAvailability addAvailability(@RequestBody OrganizationAvailability organizationAvailability) {
        return organizationService.saveOrganizationAvailability(organizationAvailability);
    }

    @Operation(summary = "Save Organization Availability")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationAvailability.class))})})
    @GetMapping("/availability")
    public Optional<OrganizationAvailability> getAvailability() {
        return organizationService.getOrganizationAvailability();
    }

    @Operation(summary = "Email Unsubscribe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Organization.class))})})
    @PatchMapping("/emailUnsubscribe")
    public Organization emailSubscribe(@RequestParam String email) {
        return organizationService.emailUnsubscribe(email);
    }

    @Operation(summary = "Get Partners Details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PartnerInviteResponse.class))})})
    @GetMapping("/partner-details")
    public List<PartnerInviteResponse> getPartnerDetail(
    ) {
        return organizationService.getPartnerDetails();
    }

    @Operation(summary = "Get Partners Invite Status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PartnerInviteStatus.class))})})
    @GetMapping("/partner-invite/status")
    public PartnerInviteStatus getPartnerInviteStatus(
            @RequestParam String email, @RequestParam Long organizationId) {
        return organizationService.getPartnerInviteStatus(email, organizationId);
    }

    @Operation(summary = "Get Partners Invite Details By Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PartnerInviteResponse.class))})})
    @GetMapping("/partner-details/id")
    public PartnerInviteResponse getPartnerInviteResponse(
            @RequestParam Long id) {
        return organizationService.getPartnerDetailsById(id);
    }

    @Operation(summary = "Get Public Organization Details ")
    @GetMapping("/details")
    public PublicOrganizationResponse getPartnerInviteResponse(@RequestParam String code) {
        return organizationService.getPublicOrganizationResponse(code);
    }

    @Operation(summary = "Mark Organization Visited")
    @PostMapping("/visited")
    public VisitorOrganization markOrganizationVisited(@RequestParam Long organizationId, @RequestParam Long visitorId) {
        return organizationService.markOrganizationVisited(organizationId, visitorId);
    }


    @Operation(summary = "Get organizations with pagination and optional subsector filtering")
    @GetMapping
    public Page<OrganizationResponse> getOrganizations(
            @RequestParam(value = "subSectorsCommaSeparated", required = false) String subSectorsCommaSeparated,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return organizationService.getOrganizations(subSectorsCommaSeparated, pageable);
    }

    @Operation(summary = "Save organization to shortlist",
            description = "Add a partner organization to the shortlist for collaboration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization successfully shortlisted"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/saveShortlisting")
    public ResponseEntity<ShortlistOrganization> saveShortlist(
            @RequestBody ShortlistOrganization shortlistOrganization) {
        ShortlistOrganization saved = shortlistingService.saveShortListOrganization(shortlistOrganization);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Get shortlisted organizations",
            description = "Retrieve a paginated list of shortlisted organizations for a given organization ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shortlisted organizations fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getShortListing/{orgId}")
    public ResponseEntity<List<ShortlistedOrganizationResponse>> getShortlistedOrganizations(
            @PathVariable String orgId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<ShortlistedOrganizationResponse> shortlist =
                shortlistingService.getShortListedOrganizations(orgId, page, size);
        return ResponseEntity.ok(shortlist);
    }

    @Operation(summary = "Remove organization from shortlist",
            description = "Remove a partner organization from the shortlist.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization successfully removed from shortlist"),
            @ApiResponse(responseCode = "404", description = "Shortlisted organization not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/removeShortlisting/{shortlistedOrgId}")
    public ResponseEntity<String> removeShortlist(
            @PathVariable Long shortlistedOrgId) {

        shortlistingService.removeShortlistedOrganization(shortlistedOrgId);
        return ResponseEntity.ok("Organization removed from shortlist successfully");
    }

    @GetMapping("/discover/home")
    public ResponseEntity<Page<OrganizationCustomResponse>> searchOrganizationsByFilter(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(required = false) String sectors,
            @RequestParam(defaultValue = "false") boolean matchAll
    ) {
        log.info("Received search request -> page: {}, size: {}, filters: {}, matchAll: {}",
                page, size, sectors, matchAll);

        List<String> filterList = parseFilters(sectors);

        Page<OrganizationCustomResponse> result =
                organizationService.searchOrganizationsByFilterDto(
                        filterList, matchAll, page, size
                );

        return ResponseEntity.ok(result);
    }

    private List<String> parseFilters(String filters) {
        if (filters == null || filters.isBlank()) return List.of();
        return Arrays.stream(filters.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase) // normalize to lowercase (spec uses lowercase)
                .collect(Collectors.toList());
    }

    @GetMapping("/searchByFilter")
    public ResponseEntity<Page<OrganizationCustomResponse>> searchByFilter(
            @RequestParam(required = false) String filters,
            @RequestParam(required = false) String sectors,
            @RequestParam(required = false) String partnershipTypes,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sectorType,
            @RequestParam(required = false) String compliances,
            @RequestParam(required = false) Long mostPopular,
            @RequestParam(required = false) Long topPartner,
            @RequestParam(required = false) Boolean isPopular,
            @RequestParam(required = false) Boolean isMostActive,
            @RequestParam(required = false) Boolean isTopPartner,
            @RequestParam(required = false) Boolean isShortlisted,
            @RequestParam(required = false) Boolean isRecommended,
            @RequestParam(required = false) Boolean isHighMatchPercentage,
            @RequestParam(required = false) Boolean isLowAcknowledgeTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<String> filterList = (filters != null && !filters.isEmpty()) ?
                Arrays.asList(filters.split(",")) : Collections.emptyList();

        List<String> sectorList = (sectors != null && !sectors.isEmpty()) ?
                Arrays.asList(sectors.split(",")) : Collections.emptyList();

        List<String> partnershipList = (partnershipTypes != null && !partnershipTypes.isEmpty()) ?
                Arrays.asList(partnershipTypes.split(",")) : Collections.emptyList();

        List<String> complianceList = (compliances != null && !compliances.isEmpty())
                ? Arrays.asList(compliances.split(","))
                : Collections.emptyList();

        Specification<Organization> spec = Specification
                .where(OrganizationSpecifications.hasStatus(OrganizationStatus.ACTIVE))
                .and(OrganizationSpecifications.hasFilterIn(filterList))
                .and(OrganizationSpecifications.hasPreferredSectorIn(sectorList))
                .and(OrganizationSpecifications.hasPreferredPartnershipIn(partnershipList))
                .and(OrganizationSpecifications.hasKeywordInMultipleFields(keyword))
                .and(OrganizationSpecifications.isShortlisted(isShortlisted, Util.getOrgIdFromToken()))
                .and(OrganizationSpecifications.hasSectorType(sectorType))
                .and(OrganizationSpecifications.hasComplianceIn(complianceList))
                .and(OrganizationSpecifications.hasMostPopular(mostPopular))
                .and(OrganizationSpecifications.hasTopPartner(topPartner))
                .and(OrganizationSpecifications.hasLowAcknowledgmentTime(isLowAcknowledgeTime))
                .and(OrganizationSpecifications.isTopPartner(isTopPartner))
                .and(OrganizationSpecifications.isPopular(isPopular))
                .and(OrganizationSpecifications.isMostActive(isMostActive))
                .and(OrganizationSpecifications.hasValidLogoUrl());

        Page<Organization> result = organizationRepository.findAll(spec, PageRequest.of(page, size));
        var optOrg = organizationRepository.findById(Util.getOrgIdFromToken());

        Page<OrganizationCustomResponse> dtoPage = result.map(org -> new OrganizationCustomResponse(
                org.getId(),
                org.getName(),
                org.getAbout(),
                org.getBriefDescription(),
                org.getWebsite(),
                org.getLogoUrl(),
                org.getMeetingSuccessRate(),
                org.getAcknowledgmentTime(),
                org.getActivePartnerships(),
                org.getPipelinePartnerships(),
                org.getLegalName(),
                org.getPreferredSectors() != null ?
                        org.getPreferredSectors().stream().map(PreferredSector::getArea).toList() :
                        Collections.emptyList(),
                org.getFilters(),
                org.getPrimaryEmail(),
                offlinePartnerInviteRepository
                        .findByOrganizationIdAndEmail(Util.getOrgIdFromToken(), org.getPrimaryEmail())
                        .isPresent(),
                shortlistingService.isOrganizationShortlisted(org.getId()),
                org.getCompliances(),
                calculateMatchScore(org,optOrg.get()),
                org.getTrustpilotRating(),
                org.getTrustpilotTotalReviews()
                ));

        log.info("Returning {} organizations with selected fields", dtoPage.getTotalElements());
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/discover/dweep")
    public ResponseEntity<DweepSearchResponse> dweepSearch(@RequestParam String input) {
        log.info("Received Dweep AI search request: input={}", input);
        DweepSearchResponse response = dweepAISearch.searchOrganizations(input);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/unverified/deal")
    public ResponseEntity<Boolean> updateUnverifiedDealStatus(
            @RequestParam boolean isUnverifiedDeal) {
        boolean updatedStatus = organizationService.updateUnverifiedDealStatus(isUnverifiedDeal);
        return ResponseEntity.ok(updatedStatus);
    }

    @GetMapping("/connected/types")
    public ResponseEntity<List<IntegrationType>> getConnectedIntegrationTypes() {
        List<IntegrationType> connectedTypes = organizationService.getConnectedIntegrationTypes();
        return ResponseEntity.ok(connectedTypes);
    }

    @PatchMapping("/currency")
    public ResponseEntity<Map<String, Object>> updateOrganizationCurrency(
            @RequestParam String currency) {
        Map<String, Object> response = organizationCurrencyService.updateOrganizationCurrency(currency);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/currency")
    public ResponseEntity<Map<String, Object>> getOrganizationCurrency() {
        Map<String, Object> response = organizationCurrencyService.getOrganizationCurrency();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/compliances")
    public SharkdomApiResponse<OrganizationCompliancesResponse>
    updateOrganizationCompliances(
            @RequestBody UpdateOrganizationCompliancesRequest request
    ) {
        return organizationService.updateCompliancesByOrg(request);
    }

    @PutMapping("/{orgId}/top/partner")
    public SharkdomApiResponse<TopPartnerResponse>
    updateTopPartner(
            @PathVariable Long orgId,
            @RequestBody UpdateTopPartnerRequest request
    ) {
        return organizationService.updateTopPartnerByOrgId(orgId, request);
    }

    @PutMapping("/{orgId}/most/popular")
    public SharkdomApiResponse<MostPopularResponse>
    updateMostPopular(
            @PathVariable Long orgId,
            @RequestBody UpdateMostPopularRequest request
    ) {
        return organizationService.updateMostPopularByOrgId(orgId, request);
    }

    @GetMapping("/updated/fromTO")
    public ResponseEntity<?> getUpdatedOrganizations(
            @RequestParam("fromDate")
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date fromDate,

            @RequestParam("toDate")
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            Date toDate
    ) {
        return ResponseEntity.ok(
                organizationService.getOrganizationsUpdatedBetween(fromDate, toDate)
        );
    }

    @GetMapping("/searchPartial")
    public ResponseEntity<Page<OrganizationCustomResponse>> getMarketplaceOrganizations(
            @RequestParam(defaultValue = "1") int page,   // Start from 1
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Marketplace API called | Page: {} | Size: {}", page, size);

        // Convert 1-based page number to 0-based for Spring Data
        int pageIndex = Math.max(page - 1, 0);

        Specification<Organization> spec = Specification
                .where(OrganizationSpecifications.hasStatus(OrganizationStatus.ACTIVE))
                .and(OrganizationSpecifications.hasValidLogoUrl());

        Pageable pageable = PageRequest.of(
                pageIndex,
                size,
                Sort.by(Sort.Direction.DESC, "marketPlaceRecord") // 2 → 1 → 0
        );

        log.debug("Executing marketplace query with filters: STATUS=ACTIVE, VALID_LOGO=true, SORT=marketPlaceRecord DESC");

        Page<Organization> result = organizationRepository.findAll(spec, pageable);

        log.info("Total organizations found: {}", result.getTotalElements());
        var optOrg = organizationRepository.findById(Util.getOrgIdFromToken());
        Page<OrganizationCustomResponse> dtoPage = result.map(org -> {
            log.debug("Mapping Organization ID: {} | Marketplace Rank: {}",
                    org.getId(),
                    org.getMarketPlaceRecord()
            );

            return new OrganizationCustomResponse(
                    org.getId(),
                    org.getName(),
                    org.getAbout(),
                    org.getBriefDescription(),
                    org.getWebsite(),
                    org.getLogoUrl(),
                    org.getMeetingSuccessRate(),
                    org.getAcknowledgmentTime(),
                    org.getActivePartnerships(),
                    org.getPipelinePartnerships(),
                    org.getLegalName(),
                    org.getPreferredSectors() != null
                            ? org.getPreferredSectors()
                            .stream()
                            .map(PreferredSector::getArea)
                            .toList()
                            : Collections.emptyList(),
                    org.getFilters(),
                    org.getPrimaryEmail(),
                    false,
                    false,
                    org.getCompliances(),
                    calculateMatchScore(org,optOrg.get()),
                    org.getTrustpilotRating(),
                    org.getTrustpilotTotalReviews()
            );
        });

        log.info("Returning {} marketplace organizations", dtoPage.getNumberOfElements());

        return ResponseEntity.ok(dtoPage);
    }

    public int calculateMatchScore(Organization org1, Organization org2) {

        int score = 0;

        if (org1 == null || org2 == null) {
            return 0;
        }

        // ---------- FILTER MATCH ----------
        List<String> filters1 = org1.getFilters();
        List<String> filters2 = org2.getFilters();

        if (filters1 != null && filters2 != null && !filters1.isEmpty() && !filters2.isEmpty()) {

            Set<String> filterSet = new HashSet<>(filters1);

            for (String filter : filters2) {
                if (filterSet.contains(filter)) {
                    score += 80;
                    break;
                }
            }
        }

        // ---------- TEAM SIZE MATCH ----------
        TeamSize teamSize1 = org1.getPartnershipTeamSize();
        TeamSize teamSize2 = org2.getPartnershipTeamSize();

        if (teamSize1 != null && teamSize2 != null) {

            if (teamSize1 != TeamSize.ZERO && teamSize2 != TeamSize.ZERO) {
                score += 20;
            }
        }

        return score;
    }

    @Operation(
            summary = "Update Zoho webhook details"
    )
    @ApiResponses(value = {

            @ApiResponse(

                    responseCode = "200",

                    description = "Service responded OK",

                    content = {

                            @Content(

                                    mediaType = "application/json",

                                    schema = @Schema(
                                            implementation =
                                                    IntegrationDetails.class
                                    )
                            )
                    }
            ),

            @ApiResponse(

                    responseCode = "500",

                    description = "Internal server error!",

                    content = @Content
            )
    })
    @PostMapping(value = "/zoho/webhook-details")
    public IntegrationDetails updateZohoWebhookDetails(
            @Valid
            @RequestBody
            ZohoWebhookDetailsRequest request

    ) {

        return organizationService
                .updateZohoWebhookDetails(
                        request.organizationId(),request.apiDomain());
    }
}



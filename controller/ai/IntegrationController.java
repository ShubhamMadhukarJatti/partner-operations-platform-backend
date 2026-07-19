package com.sharkdom.controller.ai;

import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.entity.ai.PersonaVersioning;
import com.sharkdom.model.ai.PersonaMode;
import com.sharkdom.model.ai.ZohoToken;
import com.sharkdom.pipedrive.dto.CreatePersonRequest;
import com.sharkdom.pipedrive.dto.CreatePersonResponse;
import com.sharkdom.pipedrive.dto.PersonFieldsResponse;
import com.sharkdom.pipedrive.dto.PersonsResponse;
import com.sharkdom.pipedrive.service.PipeDriveService;
import com.sharkdom.salesforce.dto.SalesforceDescribeResponse;
import com.sharkdom.salesforce.dto.SalesforceQueryResponse;
import com.sharkdom.salesforce.service.SalesforceService;
import com.sharkdom.service.ai.*;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@RestController

@CrossOrigin
@Slf4j
@RequestMapping("/integration")
public class IntegrationController {

    private final HubspotService hubspotService;
    private final ZohoService zohoServiceAi;
    private final IntegrationService integrationService;
    private final PipeDriveService pipeDriveService;
    private final SalesforceService salesforceService;
    private final PersonaService personaService;
    private final OverlapScheduler overlapScheduler;

    public IntegrationController(HubspotService hubspotService, ZohoService zohoServiceAi, IntegrationService integrationService, PipeDriveService pipeDriveService, SalesforceService salesforceService, PersonaService personaService, OverlapScheduler overlapScheduler) {
        this.hubspotService = hubspotService;
        this.zohoServiceAi = zohoServiceAi;
        this.integrationService = integrationService;
        this.pipeDriveService = pipeDriveService;
        this.salesforceService = salesforceService;
        this.personaService = personaService;
        this.overlapScheduler = overlapScheduler;
    }

    @Operation(summary = "Hubspot details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/hubspot")
    public Map<Object, Object> getPersonaDetails(@RequestParam Long organizationId, @RequestParam String fields) {
        return hubspotService.getDetails(organizationId, fields);
    }

    @Operation(summary = "Hubspot details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @GetMapping(path = "/hubspot/limit/after")
    public Map<Object, Object> getPersonaDetailsLimitAndAfter(
            @RequestParam Long organizationId,
            @RequestParam String fields,
            @RequestParam(required = false) String after,
            @RequestParam(required = false, defaultValue = "10") Long limit) {

        return hubspotService.getDetailsWithLimitAndAfter(
                organizationId,
                fields,
                after,
                limit
        );
    }

    @Operation(summary = "Hubspot properties")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/deal-properties")
    public Map<Object, Object> getDealProperties() {
        Long organizationId= Util.getOrgIdFromToken();
        log.info("Request received to fetch HubSpot deal properties for organizationId: {}", organizationId);
        return hubspotService.getDealProperties(organizationId);
    }

    @Operation(summary = "Hubspot Fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/hubspot/fields")
    public List<String> getHubspotFields() {
        return hubspotService.getContacts();
    }

    @Operation(summary = "Zoho Fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/zoho/fields")
    public List<String> getZohoFields() throws URISyntaxException {
        return zohoServiceAi.getFields();
    }

    @Operation(summary = "Zoho Data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/zoho/data")
    public List<Map<String, Object>> getZohoData() throws URISyntaxException {
        return zohoServiceAi.getData();
    }

    @Operation(summary = "Zoho Token details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/zoho/token")
    public ZohoToken generateToken(@RequestParam String authCode) throws URISyntaxException {
        return zohoServiceAi.generateToken(authCode);
    }

    @Operation(summary = "Docusign Token details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Generated successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/docusign/token")
    public Map generateDocusign(@RequestParam String authCode) {
        return zohoServiceAi.generateDocusignToken(authCode);
    }

    @Operation(summary = "Delete Integration")
    @DeleteMapping()
    public Map<String, String> deleteIntegration(String userId, IntegrationType integrationType) {
        integrationService.deleteIntegration(userId, integrationType);
        return Map.of("message", "Integration deleted successfully");
    }

    @Operation(summary = "pipedrive Fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/pipedrive/fields")
    public ResponseEntity<PersonFieldsResponse> getPipedriveFields() throws URISyntaxException {
        return ResponseEntity.ok(pipeDriveService.getContacts());
    }

    @Operation(summary = "PipeDrive Data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping(path = "/pipedrive/data")
    public ResponseEntity<PersonsResponse> getPipeDriveData() throws URISyntaxException {
        return ResponseEntity.ok(pipeDriveService.getDetails());
    }

    @Operation(summary = "PipeDrive Person Creation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Creation successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/pipedrive/create/person")
    public ResponseEntity<CreatePersonResponse> createPerson(@RequestBody CreatePersonRequest createPersonRequest) {
        CreatePersonResponse response = pipeDriveService.createPerson(createPersonRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Salesforce Contacts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched contacts successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @GetMapping("/salesforce/contacts")
    public ResponseEntity<?> getContacts(@RequestParam List<String> fields) {
        try {
            SalesforceQueryResponse response = salesforceService.getContacts(fields);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Failed to fetch contacts: " + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Get Salesforce Contact Object Description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched description successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @GetMapping("/salesforce/contacts/describe")
    public ResponseEntity<?> getDescription() {
        try {
            SalesforceDescribeResponse response = salesforceService.getDescription();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Failed to fetch description: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/disconnect/{integrationType}")
    public ResponseEntity<SharkdomApiResponse<String>> disconnectIntegration(
            @PathVariable IntegrationType integrationType) {

        personaService.removeLatestPersonaAndIntegrationFromCRM(integrationType);

        return ResponseEntity.ok(
                new SharkdomApiResponse<>(
                        true,
                        "Integration disconnected successfully",
                        integrationType.name()
                )
        );
    }

    @Operation(summary = "Hubspot deals properties values")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/deals")
    public ResponseEntity<?> getDeals(
            @RequestParam String fields) {
        var organizationId = Util.getOrgIdFromToken();
        log.info("Request received to fetch HubSpot deals for organizationId: {}", organizationId);
        return ResponseEntity.ok(
                hubspotService.getDeals(organizationId, fields)
        );
    }

    @Operation(summary = "Hubspot deals properties values after and limit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/deals/after/limit")
    public ResponseEntity<?> getDealsAfterAndLimit(
            @RequestParam String fields,
            @RequestParam String after,
            @RequestParam Long limit) {
        var organizationId = Util.getOrgIdFromToken();
        log.info("Request received to fetch HubSpot deals for organizationId: {}", organizationId);
        return ResponseEntity.ok(
                hubspotService.getDealsAfterAndLimit(organizationId, fields,after,limit)
        );
    }

    @Operation(summary = "Hubspot company properties")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/company-properties")
    public ResponseEntity<?> getCompanyProperties(@RequestParam Long organizationId) {

        log.info("Request received to fetch HubSpot company properties for organizationId: {}", organizationId);

        return ResponseEntity.ok(
                hubspotService.getCompanyProperties(organizationId)
        );
    }

    @Operation(summary = "Hubspot company properties values")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/companies")
    public ResponseEntity<?> getCompanies(
            @RequestParam String fields) {

        var organizationId = Util.getOrgIdFromToken();
        log.info("Request received to fetch HubSpot companies for organizationId: {}", organizationId);

        return ResponseEntity.ok(
                hubspotService.getCompanies(organizationId, fields)
        );
    }

    @Operation(summary = "Hubspot company properties values")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/companies/after/limit")
    public ResponseEntity<?> getCompaniesAfterAndLimit(
            @RequestParam String fields,
            @RequestParam String after,
            @RequestParam Long limit
    ) {

        var organizationId = Util.getOrgIdFromToken();
        log.info("Request received to fetch HubSpot companies for organizationId: {}", organizationId);

        return ResponseEntity.ok(
                hubspotService.getCompaniesAfterAndLimit(organizationId, fields,after,limit)
        );
    }

    @Operation(summary = "Hubspot company association with deal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/deal/{dealId}/companies")
    public ResponseEntity<?> getDealCompanyAssociations(
            @PathVariable String dealId) {
        var organizationId = Util.getOrgIdFromToken();
        return ResponseEntity.ok(
                hubspotService.getDealCompanyAssociations(organizationId, dealId)
        );
    }


    @Operation(summary = "Hubspot company association with contact")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/contact/{contactId}/companies")
    public ResponseEntity<?> getContactCompanyAssociations(
            @PathVariable String contactId) {
        var organizationId = Util.getOrgIdFromToken();
        return ResponseEntity.ok(
                hubspotService.getContactCompanyAssociations(organizationId, contactId)
        );
    }

    @Operation(
            summary = "Fetch HubSpot Metadata",
            description = "This API fetches HubSpot metadata using the organizationId extracted from the authentication token. "
                    + "It returns deals properties, contacts properties, and companies properties in a single response."
    )
    @GetMapping("/hubspot/metadata")
    public SharkdomApiResponse<Map<String, Object>> getHubspotMetadata() {
        return new SharkdomApiResponse<>(
                true,
                "HubSpot metadata fetched successfully",
                hubspotService.getHubspotMetadata()
        );
    }

    @Operation(
            summary = "Create HubSpot Contact",
            description = "This API creates a new contact in HubSpot using the organizationId extracted from the authentication token. "
                    + "It accepts properties and optional associations in request body."
    )
    @PostMapping("/hubspot/contact/create")
    public SharkdomApiResponse<Map<Object, Object>> createHubspotContact(
            @RequestBody Map<String, Object> payload
    ) {

        return new SharkdomApiResponse<>(
                true,
                "HubSpot contact created successfully",
                hubspotService.createContact(payload)
        );
    }

    @Operation(summary = "Get Salesforce Accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched accounts successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @GetMapping("/salesforce/accounts")
    public ResponseEntity<?> getAccounts(@RequestParam List<String> fields) {
        try {
            Map<String, Object> response = salesforceService.getAccounts(fields);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Failed to fetch accounts: " + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Get Salesforce Accounts by UserId")
    @GetMapping("/salesforce/accounts/{userId}")
    public ResponseEntity<?> getAccountsByUserId(
            @PathVariable String userId,
            @RequestParam List<String> fields
    ) {
        try {
            Map<String, Object> response =
                    salesforceService.getAccountsByUserId(userId, fields);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"Failed to fetch accounts: " + e.getMessage() + "\"}");
        }
    }

    @Operation(summary = "Get Salesforce Account Description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched account description successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @GetMapping("/salesforce/accounts/describe")
    public ResponseEntity<?> getAccountDescription() {
        try {
            Map<String, Object> response = salesforceService.getAccountDescription();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to fetch account description",
                            "message", e.getMessage()
                    ));
        }
    }

    @Operation(summary = "Get Salesforce Account Description by UserId")
    @GetMapping("/salesforce/accounts/{userId}/describe")
    public ResponseEntity<?> getAccountDescriptionByUserId(@PathVariable String userId) {
        try {
            Map<String, Object> response =
                    salesforceService.getAccountDescriptionByUserId(userId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to fetch account description",
                            "message", e.getMessage()
                    ));
        }
    }

    @Operation(summary = "Get Salesforce Opportunities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched opportunities successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @GetMapping("/salesforce/opportunities")
    public ResponseEntity<?> getOpportunities(@RequestParam List<String> fields) {
        try {
            Map<String, Object> response = salesforceService.getOpportunities(fields);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to fetch opportunities",
                            "message", e.getMessage()
                    ));
        }
    }



    @Operation(summary = "Get Salesforce Opportunities by UserId")
    @GetMapping("/salesforce/opportunities/{userId}")
    public ResponseEntity<?> getOpportunitiesByUserId(
            @PathVariable String userId,
            @RequestParam List<String> fields
    ) {
        try {
            Map<String, Object> response =
                    salesforceService.getOpportunitiesByUserId(userId, fields);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to fetch opportunities",
                            "message", e.getMessage()
                    ));
        }
    }



    @Operation(summary = "Get Salesforce Opportunity Description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched opportunity description successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @GetMapping("/salesforce/opportunities/describe")
    public ResponseEntity<?> getOpportunityDescription() {
        try {
            Map<String, Object> response =
                    salesforceService.getOpportunityDescription();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to fetch opportunity description",
                            "message", e.getMessage()
                    ));
        }
    }


    @Operation(summary = "Get Salesforce Opportunity Description by UserId")
    @GetMapping("/salesforce/opportunities/{userId}/describe")
    public ResponseEntity<?> getOpportunityDescriptionByUserId(
            @PathVariable String userId
    ) {
        try {
            Map<String, Object> response =
                    salesforceService.getOpportunityDescriptionByUserId(userId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to fetch opportunity description",
                            "message", e.getMessage()
                    ));
        }
    }

    @Operation(summary = "Get Salesforce Contact By Id using UserId")
    @GetMapping("/salesforce/contacts/{userId}/{contactId}")
    public ResponseEntity<?> getContactByIdByUserId(
            @PathVariable String userId,
            @PathVariable String contactId
    ) {
        try {
            Map<String, Object> response =
                    salesforceService.getContactByIdByUserId(
                            userId,
                            contactId
                    );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to fetch contact details by userId",
                            "message", e.getMessage()
                    ));
        }
    }


    @Operation(summary = "Get Salesforce Contact By Id")
    @GetMapping("/salesforce/contacts/{contactId}")
    public ResponseEntity<?> getContactById(
            @PathVariable String contactId
    ) {
        try {
            Map<String, Object> response =
                    salesforceService.getContactById(contactId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to fetch contact details",
                            "message", e.getMessage()
                    ));
        }
    }

    @Operation(summary = "Get Salesforce Opportunity By Id using UserId")
    @GetMapping("/salesforce/opportunities/{userId}/{opportunityId}")
    public ResponseEntity<?> getOpportunityByIdByUserId(
            @PathVariable String userId,
            @PathVariable String opportunityId
    ) {
        try {
            Map<String, Object> response =
                    salesforceService.getOpportunityByIdByUserId(
                            userId,
                            opportunityId
                    );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to fetch opportunity details by userId",
                            "message", e.getMessage()
                    ));
        }
    }


    @Operation(summary = "Get Salesforce Opportunity By Id")
    @GetMapping("/salesforce/opportunities/{opportunityId}")
    public ResponseEntity<?> getOpportunityById(
            @PathVariable String opportunityId
    ) {
        try {
            Map<String, Object> response =
                    salesforceService.getOpportunityById(opportunityId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to fetch opportunity details",
                            "message", e.getMessage()
                    ));
        }
    }


    @Operation(summary = "Fetch HubSpot company by companyId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @GetMapping("/hubspot/companies/{companyId}")
    public ResponseEntity<?> getCompanyById(
            @PathVariable String companyId,
            @RequestParam String fields
    ) {

        var organizationId = Util.getOrgIdFromToken();

        log.info(
                "Request received to fetch HubSpot company by id: {} for organizationId: {}",
                companyId,
                organizationId
        );

        return ResponseEntity.ok(
                hubspotService.getCompanyById(
                        organizationId,
                        companyId,
                        fields
                )
        );
    }

    @Operation(summary = "Fetch HubSpot contact by contactId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @GetMapping("/hubspot/contacts/{contactId}")
    public ResponseEntity<?> getContactById(
            @PathVariable String contactId,
            @RequestParam String fields
    ) {

        var organizationId = Util.getOrgIdFromToken();

        log.info(
                "Request received to fetch HubSpot contact by id: {} for organizationId: {}",
                contactId,
                organizationId
        );

        return ResponseEntity.ok(
                hubspotService.getContactById(
                        organizationId,
                        contactId,
                        fields
                )
        );
    }

    @Operation(summary = "Fetch HubSpot deal contact associations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @GetMapping("/hubspot/deals/{dealId}/contacts")
    public ResponseEntity<?> getDealContactAssociations(
            @PathVariable String dealId
    ) {

        var organizationId = Util.getOrgIdFromToken();

        log.info(
                "Request received to fetch contacts associated with dealId: {} for organizationId: {}",
                dealId,
                organizationId
        );

        return ResponseEntity.ok(
                hubspotService.getDealContactAssociations(
                        organizationId,
                        dealId
                )
        );
    }

    @Operation(summary = "Fetch HubSpot deal company associations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @GetMapping("/hubspot/deals/{dealId}/companies")
    public ResponseEntity<?> getHubspotDealCompanyAssociations(
            @PathVariable String dealId
    ) {

        var organizationId = Util.getOrgIdFromToken();

        log.info(
                "Request received to fetch companies associated with dealId: {} for organizationId: {}",
                dealId,
                organizationId
        );

        return ResponseEntity.ok(
                hubspotService.getDealCompanyAssociationsV1(
                        organizationId,
                        dealId
                )
        );
    }

    @Operation(summary = "Get Salesforce Account By Id By User Id")
    @GetMapping("/salesforce/users/{userId}/accounts/{accountId}")
    public ResponseEntity<?> getAccountByIdByUserId(
            @PathVariable String userId,
            @PathVariable String accountId
    ) {
        try {
            Map<String, Object> response =
                    salesforceService.getAccountByIdByUserId(
                            userId,
                            accountId
                    );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to fetch account details",
                            "message", e.getMessage()
                    ));
        }
    }

    @Operation(summary = "Get Salesforce Account By Id")
    @GetMapping("/salesforce/accounts/{accountId}")
    public ResponseEntity<?> getAccountById(
            @PathVariable String accountId
    ) {
        try {
            Map<String, Object> response =
                    salesforceService.getAccountById(accountId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to fetch account details",
                            "message", e.getMessage()
                    ));
        }
    }

    @Operation(summary = "Get Full Salesforce Contact By Id")
    @GetMapping("/salesforce/contacts/object/{contactId}")
    public ResponseEntity<?> getContactObjectById(
            @PathVariable String contactId
    ) {
        try {
            Map<String, Object> response =
                    salesforceService.getFullContactObjectById(contactId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to fetch contact details",
                            "message", e.getMessage()
                    ));
        }
    }

    @Operation(summary = "Get HubSpot Owner By Id")
    @GetMapping("/hubspot/owners/{ownerId}")
    public ResponseEntity<?> getOwnerById(
            @PathVariable String ownerId
    ) {
        try {

            Long organizationId = Util.getOrgIdFromToken();

            Map<Object, Object> response =
                    hubspotService.getOwnerById(
                            organizationId,
                            ownerId
                    );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to fetch HubSpot owner details",
                            "message", e.getMessage()
                    ));
        }
    }

    @Operation(summary = "Zoho Deals Data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @GetMapping(path = "/zoho/deals/data")
    public List<Map<String, Object>> getZohoDealsData()
            throws URISyntaxException {

        return zohoServiceAi.getDealsData();
    }

    @Operation(summary = "Zoho Deals Fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @GetMapping(path = "/zoho/deals/fields")
    public List<String> getZohoDealsFields()
            throws URISyntaxException {

        return zohoServiceAi.getDealsFields();
    }

    @Operation(summary = "Zoho Accounts Data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @GetMapping(path = "/zoho/accounts/data")
    public List<Map<String, Object>> getZohoAccountsData()
            throws URISyntaxException {

        return zohoServiceAi.getAccountsData();
    }

    @Operation(summary = "Zoho Accounts Fields")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)
    })
    @GetMapping(path = "/zoho/accounts/fields")
    public List<String> getZohoAccountsFields()
            throws URISyntaxException {

        return zohoServiceAi.getAccountsFields();
    }

    @Operation(summary = "Zoho Contacts Fields")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Fetched successfully."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error!",
                    content = @Content
            )
    })
    @GetMapping(path = "/zoho/contacts/fields")
    public List<String> getZohoContactsFields()
            throws URISyntaxException {

        return zohoServiceAi.getContactsFields();
    }

    @Operation(summary = "Zoho Contacts Data")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Fetched successfully."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error!",
                    content = @Content
            )
    })
    @GetMapping(path = "/zoho/contacts/data")
    public List<Map<String, Object>> getZohoContactsData()
            throws URISyntaxException {

        return zohoServiceAi.getContactsData();
    }

    @Operation(summary = "Get Zoho Account ID By Account Name")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Fetched successfully."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error!",
                    content = @Content
            )
    })
    @GetMapping(path = "/zoho/account/id")
    public String getAccountIdByAccountName(
            @RequestParam String accountName
    ) throws URISyntaxException {

        return zohoServiceAi.getAccountIdByAccountName(accountName);
    }

    @Operation(summary = "Get Account By Account Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!")
    })
    @GetMapping(path = "/zoho/account/data")
    public Map<String, Object> getAccountByAccountId(
            @RequestParam String accountId
    ) throws URISyntaxException {

        return zohoServiceAi.getAccountByAccountId(accountId);
    }

    @Operation(summary = "Get Contact Id By Contact Name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!")
    })
    @GetMapping(path = "/zoho/contact/id")
    public String getContactIdByContactName(
            @RequestParam String contactName
    ) throws URISyntaxException {

        return zohoServiceAi.getContactIdByContactName(contactName);
    }


    @Operation(summary = "Get Contact By Contact Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!")
    })
    @GetMapping(path = "/zoho/contact/data")
    public Map<String, Object> getContactByContactId(
            @RequestParam String contactId
    ) throws URISyntaxException {

        return zohoServiceAi.getContactByContactId(contactId);
    }

    @Operation(summary = "Get Deal Id By Deal Name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!")
    })
    @GetMapping(path = "/zoho/deal/id")
    public String getDealIdByDealName(
            @RequestParam String dealName
    ) throws URISyntaxException {

        return zohoServiceAi.getDealIdByDealName(dealName);
    }


    @Operation(summary = "Get Deal By Deal Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetched successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!")
    })
    @GetMapping(path = "/zoho/deal/data")
    public Map<String, Object> getDealByDealId(
            @RequestParam String dealId
    ) throws URISyntaxException {

        return zohoServiceAi.getDealByDealId(dealId);
    }
}
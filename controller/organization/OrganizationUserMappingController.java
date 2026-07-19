package com.sharkdom.controller.organization;

import com.github.fge.jsonpatch.JsonPatch;
import com.sharkdom.constants.organization.OrgUserMappingStatus;
import com.sharkdom.dto.OrganizationUserMappingResponseDTO;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organization.OrganizationUserMapping;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.model.organization.OrganizationUserMappingResponse;
import com.sharkdom.model.organization.OrganizationWithOrganizationMappingResponse;
import com.sharkdom.service.organization.OrganizationUserMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/orgUserMapping")
@Slf4j
public class OrganizationUserMappingController {

    @Autowired
    private OrganizationUserMappingService organizationUserMappingService;

    @Operation(summary = "Create new organization user mapping")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New organization  user mapping created successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationUserMapping.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("")
    public ResponseEntity<OrganizationUserMapping> create(@RequestBody OrganizationUserMapping created) {
        return ResponseEntity.ok(organizationUserMappingService.create(created));
    }

    @Operation(summary = "Update an existing organization user mapping")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the organization user mapping and updated it.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationUserMapping.class))}),
            @ApiResponse(responseCode = "404", description = "Organization user mapping not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PutMapping("")
    public ResponseEntity<OrganizationUserMapping> update(@RequestBody OrganizationUserMapping updated) throws Exception {
        return ResponseEntity.ok(organizationUserMappingService.update(updated));
    }

    @Operation(summary = "Get all users of Organization by organization id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the users.", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OrganizationUserMappingResponse.class)))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/allByOrganizationId")
    public ResponseEntity<List<OrganizationUserMappingResponse>> findAllUsersByOrganizationId(@RequestParam(name = "id") long id)
            throws Exception {
        return ResponseEntity.ok(organizationUserMappingService.findAllByOrganizationId(id));
    }

    @Operation(summary = "Get all users of Organization by organization id and status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the users.", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OrganizationUserMappingResponse.class)))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/allByOrganizationIdAndStatus")
    public ResponseEntity<List<OrganizationUserMappingResponse>> findAllUsersByOrganizationIdAndStatus(@RequestParam(name = "id") long id,
                                                                                                       @RequestParam(name = "status") OrgUserMappingStatus status)
            throws Exception {
        return ResponseEntity.ok(organizationUserMappingService.findAllByOrganizationIdAndStatus(id, status));
    }

    @Operation(summary = "Get all organizations by userId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the organizations.", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Organization.class)))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/allOrganizationsByUserId")
    public ResponseEntity<List<Organization>> findAllOrganizationsByUserId(@RequestParam(name = "userId") String userId)
            throws Exception {
        return ResponseEntity.ok(organizationUserMappingService.findAllOrganizationsByUserId(userId, OrgUserMappingStatus.ACTIVE));
    }

    @Operation(summary = "Get all organizations and OrganizationUserMappings by userId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the organizations.", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OrganizationWithOrganizationMappingResponse.class)))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/allOrganizationMappingsByUserId")
    public ResponseEntity<List<OrganizationWithOrganizationMappingResponse>> findAllOrganizationMappingsByUserId(HttpServletRequest request, @RequestParam(name = "userId") String userId)
            throws Exception {
        return ResponseEntity.ok(organizationUserMappingService.findAllOrganizationsWithOrganizationMappingsByUserId(request, userId));
    }

    @Operation(summary = "Change status of existing organization and user mapping")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the organization user mapping and changed status.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationUserMapping.class))}),
            @ApiResponse(responseCode = "404", description = "Organization user mapping not found with given Id!", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("/changeStatus")
    public ResponseEntity<OrganizationUserMapping> changeStatus(@RequestParam(name = "organizationId") long id,
                                                                @RequestParam(name = "userId") String userId,
                                                                @RequestParam(name = "status") OrgUserMappingStatus status
    ) throws ResourceNotFoundException {
        return ResponseEntity.ok(organizationUserMappingService.changeStatus(id, userId, status));
    }

    @Operation(summary = "Approve or reject a org_user_mapping request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Action successful.", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = String.class)))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/request/{requestId}/{approverUserId}/{action}")
    public void actionOnRequest(@PathVariable String requestId, @PathVariable String approverUserId, @PathVariable String action) {
        log.info("got org_user_mapping approval request for requestId " + requestId + ", approverUserId " + approverUserId + " and action is " + action);
        //TODO change status in mapping table and send the new user an email
        //TODO when receive a request check if the request in db is already approved or rejected or not if not only then proceed otherwise do nothing
    }

    @Operation(summary = "Use json patch to partially update an OrganizationUserMapping, for more details refer https://www.baeldung.com/spring-rest-json-patch ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OrganizationUserMapping updated successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationUserMapping.class))}),
            @ApiResponse(responseCode = "404", description = "OrganizationUserMapping not found with given filters", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PatchMapping(path = "", consumes = "application/json-patch+json")
    public ResponseEntity<OrganizationUserMapping> updateOrganizationUserMapping(@RequestParam(name = "organizationId") long organizationId,
                                                                                 @RequestParam(name = "userId") String userId,
                                                                                 @Parameter(description = "MyDto") @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                                                                                         @ExampleObject(name = "Sending one replace and one add operation", value = "[\r\n"
                                                                                                 + "    {\"op\":\"replace\",\"path\":\"/designation\",\"value\":\"CTO\"},\r\n"
                                                                                                 + "    {\"op\":\"add\",\"path\":\"/role\",\"value\":\"ADMIN\"}\r\n" + "]"),
                                                                                         @ExampleObject(name = "Sending remove operation to remove briefDescription", value = "[{\"op\":\"remove\",\"path\":\"/designation\"}]")})) @RequestBody JsonPatch patch)
            throws Exception {
        log.info("Received patch request for OrganizationUserMapping with organizationId: " + organizationId + " , userId: " + userId + " and json: " + patch);
        return ResponseEntity.ok(organizationUserMappingService.patch(organizationId, userId, patch));
    }

    @GetMapping("/team/section")
    public ResponseEntity<List<OrganizationUserMappingResponseDTO>> getAllUsersByOrganization() {
        log.info("Received request to fetch all organization users with roles");

        List<OrganizationUserMappingResponseDTO> responseList =
                organizationUserMappingService.findAllByOrganization();

        log.info("Returning {} organization user records", responseList.size());
        return ResponseEntity.ok(responseList);
    }


}

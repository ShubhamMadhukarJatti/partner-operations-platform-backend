package com.sharkdom.controller.organization;

import com.github.fge.jsonpatch.JsonPatch;
import com.sharkdom.entity.organization.OrganizationFollower;
import com.sharkdom.service.organization.OrganizationFollowerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/organizationFollower")
@Slf4j
public class OrganizationFollowerController {

    @Autowired
    private OrganizationFollowerService organizationFollowerService;

    @Operation(summary = "Update an existing organizationFollower by sending organizationFollower json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the organizationFollower and updated it.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationFollower.class))}),
            @ApiResponse(responseCode = "404", description = "OrganizationFollower not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})

    @PutMapping("")
    public ResponseEntity<OrganizationFollower> update(@RequestBody OrganizationFollower updated) throws Exception {
        return ResponseEntity.ok(organizationFollowerService.update(updated));
    }

    @Operation(summary = "Create new organizationFollower by sending user json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New organizationFollower created successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationFollower.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping("")
    public ResponseEntity<OrganizationFollower> create(@RequestBody OrganizationFollower created) {
        return ResponseEntity.ok(organizationFollowerService.create(created));
    }

    @Operation(summary = "Get All organizationFollower info by followerOrganizationId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found for organizationFollower.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationFollower.class))}),
            @ApiResponse(responseCode = "404", description = "OrganizationFollower not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/followerId")
    public ResponseEntity<Page<OrganizationFollower>> findByFollowerId(@RequestParam(name = "followerOrganizationId") long followerOrganizationId,
                                                                       @RequestParam(value = "page", defaultValue = "0") int page,
                                                                       @RequestParam(value = "size", defaultValue = "20") int size) throws Exception {
        return ResponseEntity.ok(organizationFollowerService.findByFollowerId(followerOrganizationId, page, size));
    }

    @Operation(summary = "Get all OrganizationFollower info by organizationId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the organizationFollower.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationFollower.class))}),
            @ApiResponse(responseCode = "404", description = "OrganizationFollower not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/organizationId")
    public ResponseEntity<Page<OrganizationFollower>> findByOrganisationId(@RequestParam(name = "organizationId") long organizationId,
                                                                           @RequestParam(value = "page", defaultValue = "0") int page,
                                                                           @RequestParam(value = "size", defaultValue = "20") int size) throws Exception {
        return ResponseEntity.ok(organizationFollowerService.findByOrganizationId(organizationId, page, size));
    }

    @Operation(summary = "Use json patch to partially update an organizationFollower")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OrganizationFollower updated successfully.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationFollower.class))}),
            @ApiResponse(responseCode = "404", description = "OrganizationFollower not found with given id", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PatchMapping(path = "", consumes = "application/json-patch+json")
    public ResponseEntity<OrganizationFollower> patchFollowerById(@RequestParam(name = "organizationId") long organizationId,
                                                                  @RequestParam(name = "followerOrganizationId") long followerOrganizationId,
                                                                  @Parameter(description = "MyDto") @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                                                                          @ExampleObject(name = "Sending one replace and one add operation", value = "[\r\n"
                                                                                  + "    {\"op\":\"replace\",\"path\":\"/organizationId\",\"value\":\"987\"},\r\n"
                                                                                  + "    {\"op\":\"add\",\"path\":\"/followerUserId\",\"value\":\"1234\"}\r\n" + "]")})) @RequestBody JsonPatch patch)
            throws Exception {
        log.info("Received patch request for " + organizationId + " followerOrganizationId " + followerOrganizationId + " and json: " + patch);
        return ResponseEntity.ok(organizationFollowerService.patchByFollowerId(organizationId, followerOrganizationId, patch));
    }

    @Operation(summary = "Delete an OrganizationFollower by organizationId and followerOrganizationId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted organizationFollower.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = OrganizationFollower.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @DeleteMapping("")
    public ResponseEntity<HttpStatus> delete(
            @RequestParam(name = "organizationId") long organizationId,
            @RequestParam(value = "followerOrganizationId") long followerOrganizationId,
            @RequestParam(value = "followStoppedByUserId") String followStoppedByUserId) {
        return ResponseEntity.ok(organizationFollowerService.delete(organizationId, followerOrganizationId, followStoppedByUserId));
    }

    @Operation(summary = "get an OrganizationFollower by organizationId and followerOrganizationId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found organizationFollower.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("")
    public ResponseEntity<Boolean> findByOrganizationIdAndFollowerOrganizationId(
            @RequestParam(name = "organizationId") long organizationId,
            @RequestParam(value = "followerOrganizationId") long followerOrganizationId) {
        return ResponseEntity.ok(organizationFollowerService.findBooleanValueByOrganizationIdAndFollowerOrganizationId(organizationId, followerOrganizationId));
    }

    @Operation(summary = "Check Relation between two organizations ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "found relation.", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/check")
    public ResponseEntity<Boolean> findOrganizationAAndOrganizationB(
            @RequestParam(name = "organizationA") long organizationA,
            @RequestParam(value = "organizationB") long organizationB) {
        return ResponseEntity.ok(organizationFollowerService.checkRelationBetweenOrganization(organizationA, organizationB));
    }

}

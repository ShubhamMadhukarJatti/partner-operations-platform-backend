package com.sharkdom.service;

import com.sharkdom.service.organization.OrganizationUserMappingRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller doesn't have any security, think twice before exposing any endpoint here
 */
@RestController

@Slf4j
@RequestMapping("/service")
public class OpenController {

    @Autowired
    OrganizationUserMappingRequestService organizationUserMappingRequestService;

    private final String redirectUrl = "<meta http-equiv=\"Refresh\" content=\"0; url='https://www.sharkdom.com/dashboard'\" />";

    @Operation(summary = "Approve or reject a org_user_mapping request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Action successful.", content = {
                    @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = String.class)))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/approveUser/{requestId}/{actionedByUserFk}/{action}")
    public String actionOnRequest(@PathVariable String requestId, @PathVariable long actionedByUserFk, @PathVariable String action) {
        log.info("got org_user_mapping approval request for requestId " + requestId + ", actionedByUserFk " + actionedByUserFk + " and action is " + action);
        organizationUserMappingRequestService.action(requestId, actionedByUserFk, action);
        return redirectUrl;
    }

    @GetMapping("")
    public String base() {
        return redirectUrl;
    }
}

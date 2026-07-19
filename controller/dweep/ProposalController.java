package com.sharkdom.controller.dweep;

import com.sharkdom.entity.organizationcollaboration.OrganizationCollaboration;
import com.sharkdom.model.ai.SharkqQueryRequest;
import com.sharkdom.service.dweep.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dweep")
public class ProposalController {

    @Autowired
    ProposalService proposalService;

    @Operation(summary = "Send Proposal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposal sent for selected organization", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})

    @PostMapping("/sendProposal")
    public OrganizationCollaboration sendProposal(@RequestHeader("Authorization") String authorizationHeader, @RequestBody SharkqQueryRequest request) {
        String accessToken = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring(7);
        }
        return proposalService.sendProposal(request,accessToken);
    }
}


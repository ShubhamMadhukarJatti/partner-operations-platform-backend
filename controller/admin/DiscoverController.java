package com.sharkdom.controller.admin;

import com.sharkdom.entity.admin.DiscoveryEntity;
import com.sharkdom.model.admin.DiscoveryEntityResponse;
import com.sharkdom.service.admin.DiscoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/discovery")
public class DiscoverController {
    private final DiscoveryService discoveryService;

    public DiscoverController(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Operation(summary = "Add new organization")
    @PostMapping("")
    public ResponseEntity<DiscoveryEntity> addOrganization(@RequestBody DiscoveryEntity discoveryEntity) {
        return ResponseEntity.ok(discoveryService.add(discoveryEntity));
    }

    @Operation(summary = "Get all organizations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "find All organizations", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = DiscoveryEntityResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("")
    public ResponseEntity<Page<DiscoveryEntityResponse>> getDiscoveryData(
            @RequestParam String partialName, @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(discoveryService.search(partialName, page, size));
    }

}

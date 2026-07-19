package com.sharkdom.controller.perks;

import com.sharkdom.entity.perks.CountType;
import com.sharkdom.entity.perks.PerkStatus;
import com.sharkdom.entity.perks.PerksEntity;
import com.sharkdom.service.perks.PerksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/perks")
public class PerksController {
    private final PerksService perksService;

    public PerksController(PerksService perksService) {
        this.perksService = perksService;
    }

    @Operation(summary = "Add new perk")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PerksEntity.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PostMapping
    public ResponseEntity<PerksEntity> addPerk(@RequestBody PerksEntity perksEntity) {
        return ResponseEntity.ok(perksService.save(perksEntity));
    }

    @Operation(summary = "Get Perk By Status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PerksEntity.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping
    public ResponseEntity<Page<PerksEntity>> getPerk(@RequestParam PerkStatus perkStatus,
                                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                                     @RequestParam(value = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(perksService.getPerks(perkStatus, size, page));
    }

    @Operation(summary = "Update Perk Count")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PerksEntity.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PatchMapping
    public ResponseEntity<PerksEntity> updatePerk(@RequestParam Long id, @RequestParam CountType type) {
        return ResponseEntity.ok(perksService.updateCount(id, type));
    }
}

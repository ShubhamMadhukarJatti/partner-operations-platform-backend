package com.sharkdom.controller.credits;

import com.sharkdom.entity.credits.Credits;
import com.sharkdom.model.credits.CreditsModel;
import com.sharkdom.service.credits.CreditsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController

@CrossOrigin
@Slf4j
@RequestMapping("/credits")
public class CreditsController {
    private final CreditsService creditsService;

    public CreditsController(CreditsService creditsService) {
        this.creditsService = creditsService;
    }

    @Operation(summary = "Deduct credits")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credits deducted successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @PatchMapping
    public ResponseEntity<Credits> deductCredits(@RequestBody CreditsModel creditsModel) {
        return creditsService.deductCredits(creditsModel);
    }

    @Operation(summary = "Get credits")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credits retrieved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping
    public ResponseEntity<Credits> getCredits() {
        return creditsService.getCredits();
    }

}

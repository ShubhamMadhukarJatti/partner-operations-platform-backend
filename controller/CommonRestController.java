package com.sharkdom.controller;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.model.common.StepTracker;
import com.sharkdom.service.common.StepTrackerService;
import com.sharkdom.service.organization.WeeklyOutcomeScheduler;
import com.sharkdom.service.partnerportalsnapshot.PartnerPortalSnapShotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/onboarding/steps")
public class CommonRestController {

    @Autowired
    private StepTrackerService stepTrackerService;

    @Operation(summary = "Save or update onboarding step tracker")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Step tracker saved successfully.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = StepTracker.class))}),
            @ApiResponse(responseCode = "500", description = "Internal server error!",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<StepTracker> addOrUpdateStep(@RequestBody StepTracker stepTracker) {
        log.info("API called: Add/Update StepTracker for userId={}", stepTracker.getUserId());
        StepTracker savedTracker = stepTrackerService.addStep(stepTracker);
        return ResponseEntity.ok(savedTracker);
    }

    @Operation(summary = "Get onboarding step tracker by userId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Step tracker retrieved successfully.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = StepTracker.class))}),
            @ApiResponse(responseCode = "404", description = "Step tracker not found.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error!",
                    content = @Content)
    })
    @GetMapping("/{userId}")
    public ResponseEntity<StepTracker> getStepTracker(@PathVariable String userId) {

        if (userId == null || userId.isBlank()) {
            throw new ServiceException(ErrorMessages.SH39, "null");
        }

        StepTracker tracker = stepTrackerService.getStepTrackerByUserId(userId);
        return ResponseEntity.ok(tracker);
    }


    @Autowired
    private WeeklyOutcomeScheduler weeklyOutcomeScheduler;

    @Autowired
    private PartnerPortalSnapShotService partnerPortalSnapShotService;

    @PostMapping("/send-emails")
    public ResponseEntity<String> sendWeeklyEmails() {
        log.info("Manual API triggered to send weekly emails directly");

        weeklyOutcomeScheduler.sendEmails();

        return ResponseEntity.ok("Weekly outcome emails sent successfully!");
    }
}

package com.sharkdom.controller.scheduledJob;

import com.sharkdom.entity.scheduledJob.JobInfo;
import com.sharkdom.service.scheduledJob.ScheduledMethodScanner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/scheduler")
public class ScheduledJobController {

    @Autowired
    private ScheduledMethodScanner scheduledMethodScanner;

    @Operation(summary =  "Api returns all the scheduled methods")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved successfully."),
            @ApiResponse(responseCode = "500", description = "Internal server error!", content = @Content)})
    @GetMapping("/scheduled-methods")
    public ResponseEntity<List<JobInfo>> getAllScheduledJobs() {
        return ResponseEntity.ok(scheduledMethodScanner.getJobInfos());
    }
}

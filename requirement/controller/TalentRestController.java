package com.sharkdom.requirement.controller;

import com.sharkdom.partnertraining.dto.CoverImageUploadResponseDto;
import com.sharkdom.partnertraining.service.CourseService;
import com.sharkdom.requirement.entity.CommunityOptIn;
import com.sharkdom.requirement.entity.TalentNetwork;
import com.sharkdom.requirement.service.CommunityOptInService;
import com.sharkdom.requirement.service.TalentNetworkService;
import com.sharkdom.util.SharkdomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for Talent Network & Community Opt-In operations.
 * Handles requirement creation, listing, community subscriptions,
 * and file uploads to S3.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/talent/network")
@Tag(name = "Talent Network APIs", description = "APIs for Talent Requirements, Community Opt-In & File Upload")
public class TalentRestController {

    private final TalentNetworkService talentNetworkService;
    private final CommunityOptInService createOptInService;
    private final CourseService courseService;

    /** Create new Talent Network requirement */
    @Operation(summary = "Create Talent Requirement", description = "Creates a new talent network requirement")
    @ApiResponse(responseCode = "200", description = "Talent Network created successfully")
    @PostMapping("/requirement/create")
    public ResponseEntity<SharkdomApiResponse<TalentNetwork>> createTalentNetwork(
            @RequestBody TalentNetwork request) throws Exception {
        var res = talentNetworkService.createTalentNetwork(request);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Talent Network created successfully",res));
    }

    /** Fetch all Talent Network requirements */
    @Operation(summary = "Get Talent Requirements", description = "Fetch all talent network requirements")
    @ApiResponse(responseCode = "200", description = "Talent Networks fetched successfully")
    @GetMapping("/requirement/list")
    public ResponseEntity<SharkdomApiResponse<List<TalentNetwork>>> getAllTalentNetworks() {
        var res = talentNetworkService.getAllTalentNetworks();
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Talent Networks fetched successfully",res));
    }

    /** Create Community Opt-In */
    @Operation(summary = "Create Community Opt-In", description = "Subscribe user to community")
    @ApiResponse(responseCode = "200", description = "Successfully subscribed to community")
    @PostMapping("/save/community/opt-in")
    public ResponseEntity<SharkdomApiResponse<CommunityOptIn>> create(
            @RequestBody CommunityOptIn request) throws Exception {
        var res = createOptInService.createOptIn(request);
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Successfully subscribed to community",res));
    }

    /** Fetch all Community Opt-Ins */
    @Operation(summary = "Get Community Opt-Ins", description = "Fetch all community opt-in records")
    @ApiResponse(responseCode = "200", description = "Community opt-ins fetched successfully")
    @GetMapping("/get/all/community/opt-ins")
    public ResponseEntity<SharkdomApiResponse<List<CommunityOptIn>>> getAll() {
        var res = createOptInService.getAllOptIns();
        return ResponseEntity.ok(new SharkdomApiResponse<>(true,"Community opt-ins fetched successfully",res));
    }

    /** Upload PDF/File to S3 */
    @Operation(summary = "Upload File", description = "Uploads file to S3 and returns public URL")
    @ApiResponse(responseCode = "200", description = "File uploaded successfully")
    @PostMapping(value = "/upload/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SharkdomApiResponse<CoverImageUploadResponseDto> uploadFile(
            @Parameter(description = "File to upload", required = true)
            @RequestParam("file") MultipartFile file) {
        var res = courseService.uploadFile(file);
        return new SharkdomApiResponse<>(true,"File uploaded successfully",res);
    }
}
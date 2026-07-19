package com.sharkdom.agenticai.controller;

import com.sharkdom.agenticai.model.*;
import com.sharkdom.agenticai.service.*;
import com.sharkdom.util.SharkdomApiResponse;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
@Tag(name="Review API",description="Sharkdom Review APIs")
public class ReviewController {

    private final ReviewHealthService reviewHealthService;
    private final ReviewServiceInfoService reviewServiceInfoService;
    private final ReviewFetchService reviewFetchService;

    // ================= HEALTH =================
    @GetMapping("/health")
    @Operation(summary="Check Review Service Health",description="Check external review service health")
    public SharkdomApiResponse<ReviewHealthResponse> checkReviewHealth(){
        log.info("Check review health");
        ReviewHealthResponse res=reviewHealthService.checkHealth();
        return new SharkdomApiResponse<>(true,"Review service health fetched",res);
    }

    // ================= SERVICE INFO =================
    @GetMapping("/service-info")
    @Operation(summary="Get Review Service Info",description="Fetch service metadata")
    public SharkdomApiResponse<ReviewServiceInfoResponse> getServiceInfo(){
        log.info("Fetch review service info");
        ReviewServiceInfoResponse res=reviewServiceInfoService.getServiceInfo();
        return new SharkdomApiResponse<>(true,"Review service info fetched",res);
    }

    // ================= FETCH REVIEWS =================
    @PostMapping("/reviews")
    @Operation(summary="Fetch Organization Reviews",description="Fetch Trustpilot & G2 reviews")
    public SharkdomApiResponse<ReviewFetchResponse> fetchReviews(@Valid @RequestBody ReviewFetchRequest req){
        log.info("Fetch reviews | orgName={} | url={}",req.getOrgName(),req.getOrgWebURL());
        ReviewFetchResponse res=reviewFetchService.fetchReviews(req);
        return new SharkdomApiResponse<>(true,"Review fetch completed",res);
    }
}
package com.sharkdom.agenticai.controller;

import com.sharkdom.agenticai.model.*;
import com.sharkdom.agenticai.service.*;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/forecast")
@RequiredArgsConstructor
@Tag(name="Forecast APIs",description="Forecast, AI search & prompt history APIs")
public class ForecastController {

    private final ForecastService forecastService;
    private final DweepHealthService dweepHealthService;
    private final DweepTrainingService dweepTrainingService;
    private final SharkdomSearchService service;
    private final AiPromptHistoryService aiPromptHistoryService;

    // ================= GENERATE =================
    @Operation(summary="Generate Forecast",description="Generate AI forecast")
    @PostMapping("/generate")
    public SharkdomApiResponse<ForecastResponse> generateForecast(@Valid @RequestBody ForecastRequest req){
        log.info("Generate forecast | orgId={} | orgName={}",req.getORGid(),req.getORGname());
        ForecastResponse res=forecastService.generateForecast(req);
        return new SharkdomApiResponse<>(true,"Forecast generated successfully",res);
    }

    // ================= HEALTH =================
    @Operation(summary="Check AI Health",description="Check Dweep health")
    @GetMapping("/health")
    public SharkdomApiResponse<DweepHealthResponse> checkHealth(){
        log.info("Health check request");
        return new SharkdomApiResponse<>(true,"Dweep health fetched",dweepHealthService.checkHealth());
    }

    // ================= BATCH =================
    @Operation(summary="Batch Forecast",description="Batch forecast generation")
    @PostMapping("/batch")
    public SharkdomApiResponse<List<ForecastBatchResponse>> batchForecast(@RequestBody List<ForecastBatchRequest> req){
        log.info("Batch forecast | size={}",req.size());
        List<ForecastBatchResponse> res=forecastService.generateBatchForecast(req);
        return new SharkdomApiResponse<>(true,"Batch forecast generated",res);
    }

    // ================= TRAINING =================
    @Operation(summary="Training Examples",description="Fetch AI training examples")
    @GetMapping("/training-examples")
    public SharkdomApiResponse<TrainingExamplesResponse> getTrainingExamples(){
        log.info("Fetch training examples");
        return new SharkdomApiResponse<>(true,"Training examples fetched",dweepTrainingService.getTrainingExamples());
    }

    // ================= SEARCH PARTNERS =================
    @Operation(summary="Search Partners",description="AI partner search")
    @PostMapping("/search/partners/filters")
    public SharkdomApiResponse<QueryResponse> searchPartners(@RequestBody QueryRequest req){
        return new SharkdomApiResponse<>(true,"Search completed",service.queryPartners(req));
    }

    // ================= SEARCH FILTER =================
    @Operation(summary="Search Partner Companies",description="Search by filters with pagination")
    @GetMapping("/searchByFilter")
    public SharkdomApiResponse<Page<PartnerCompanyProfileSearchResponse>> searchByFilter(
            @RequestParam(required=false) String subsectors,
            @RequestParam(required=false) String compliances,
            @RequestParam(required=false) String keyword,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size){

        List<String> subList=subsectors!=null&&!subsectors.isEmpty()?Arrays.asList(subsectors.split(",")):Collections.emptyList();
        List<String> compList=compliances!=null&&!compliances.isEmpty()?Arrays.asList(compliances.split(",")):Collections.emptyList();

        Page<PartnerCompanyProfileSearchResponse> res=service.searchByFilter(subList,compList,keyword,page,size);

        return new SharkdomApiResponse<>(true,"Partners fetched",res);
    }

    // ================= SAVE HISTORY =================
    @Operation(summary="Save Prompt History",description="Save AI prompt history")
    @PostMapping("/history/save")
    public SharkdomApiResponse<AiPromptHistoryResponse> savePromptHistory(@RequestBody AiPromptHistoryRequest req){
        return new SharkdomApiResponse<>(true,"History saved",aiPromptHistoryService.savePromptHistory(req));
    }

    // ================= GET HISTORY =================
    @Operation(summary="Get Prompt History",description="Fetch prompt history by org")
    @GetMapping("/history/org")
    public SharkdomApiResponse<List<AiPromptHistoryResponse>> getHistoryByOrg(){
        Long orgId=Util.getOrgIdFromToken();
        return new SharkdomApiResponse<>(true,"History fetched",aiPromptHistoryService.getPromptHistoryByOrgId(orgId));
    }
}
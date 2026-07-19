package com.sharkdom.agenticai.controller;

import com.sharkdom.agenticai.model.*;
import com.sharkdom.agenticai.service.PartnerCompanyProfileService;
import com.sharkdom.util.SharkdomApiResponse;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partner-company")
@RequiredArgsConstructor
@Tag(name="Partner Company Profile API")
public class PartnerCompanyProfileController {

    private final PartnerCompanyProfileService service;

    // ================= CREATE =================
    @PostMapping
    @Operation(summary="Create Partner Company Profile")
    public SharkdomApiResponse<PartnerCompanyProfileResponse> create(@Valid @RequestBody PartnerCompanyProfileRequest req){
        return new SharkdomApiResponse<>(true,"Profile created",service.create(req));
    }

    // ================= GET ALL =================
    @GetMapping
    @Operation(summary="Get All Partner Company Profiles")
    public SharkdomApiResponse<Page<PartnerCompanyProfileResponse>> getAll(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size){
        return new SharkdomApiResponse<>(true,"Profiles fetched",service.getAll(page,size));
    }

    // ================= GET BY ID =================
    @GetMapping("/{id}")
    @Operation(summary="Get Partner Company Profile By ID")
    public SharkdomApiResponse<PartnerCompanyProfileResponse> getById(@PathVariable Long id){
        return new SharkdomApiResponse<>(true,"Profile fetched",service.getById(id));
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    @Operation(summary="Delete Partner Company Profile")
    public SharkdomApiResponse<Void> deleteById(@PathVariable Long id){
        service.deleteById(id);
        return new SharkdomApiResponse<>(true,"Profile deleted",null);
    }
}
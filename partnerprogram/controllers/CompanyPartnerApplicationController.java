package com.sharkdom.partnerprogram.controllers;

import com.sharkdom.partnerprogram.dtos.CompanyPartnerApplicationDTO;
import com.sharkdom.partnerprogram.service.CompanyPartnerApplicationService;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.SharkdomPaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company-partner-applications")
@RequiredArgsConstructor
@Slf4j
public class CompanyPartnerApplicationController {

    private final CompanyPartnerApplicationService service;

    @Operation(summary = "Create Company Partner Application")
    @PostMapping
    public SharkdomApiResponse<CompanyPartnerApplicationDTO> create(@RequestBody CompanyPartnerApplicationDTO dto) {
        return new SharkdomApiResponse<>(true, "Created successfully", service.create(dto));
    }

    @Operation(summary = "Update Company Partner Application")
    @PutMapping("/{id}")
    public SharkdomApiResponse<CompanyPartnerApplicationDTO> update(
            @PathVariable Long id,
            @RequestBody CompanyPartnerApplicationDTO dto) {
        return new SharkdomApiResponse<>(true, "Updated successfully", service.update(id, dto));
    }

    @Operation(summary = "Delete Company Partner Application")
    @DeleteMapping("/{id}")
    public SharkdomApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return new SharkdomApiResponse<>(true, "Deleted successfully", null);
    }

    @Operation(summary = "Get Company Partner Application by ID")
    @GetMapping("/{id}")
    public SharkdomApiResponse<CompanyPartnerApplicationDTO> getById(@PathVariable Long id) {
        return new SharkdomApiResponse<>(true, "Fetched successfully", service.getById(id));
    }

    @Operation(summary = "Get All Company Partner Applications with Pagination")
    @GetMapping
    public SharkdomApiResponse<SharkdomPaginatedResponse<CompanyPartnerApplicationDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return new SharkdomApiResponse<>(true, "Fetched successfully", service.getAll(page, size));
    }
}

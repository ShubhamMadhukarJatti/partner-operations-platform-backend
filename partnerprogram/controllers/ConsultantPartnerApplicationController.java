package com.sharkdom.partnerprogram.controllers;

import com.sharkdom.partnerprogram.dtos.ConsultantPartnerApplicationDTO;
import com.sharkdom.partnerprogram.service.ConsultantPartnerApplicationService;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.SharkdomPaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultant-partner-applications")
@RequiredArgsConstructor
@Slf4j
public class ConsultantPartnerApplicationController {

    private final ConsultantPartnerApplicationService service;

    @Operation(summary = "Create Consultant Partner Application")
    @PostMapping
    public SharkdomApiResponse<ConsultantPartnerApplicationDTO> create(
            @RequestBody ConsultantPartnerApplicationDTO dto) {

        log.info("Received request to create ConsultantPartnerApplication for email: {}", dto.getEmail());
        return new SharkdomApiResponse<>(true, "Created successfully", service.create(dto));
    }

    @Operation(summary = "Update Consultant Partner Application")
    @PutMapping("/{id}")
    public SharkdomApiResponse<ConsultantPartnerApplicationDTO> update(
            @PathVariable Long id,
            @RequestBody ConsultantPartnerApplicationDTO dto) {

        log.info("Received request to update ConsultantPartnerApplication with id: {}", id);
        return new SharkdomApiResponse<>(true, "Updated successfully", service.update(id, dto));
    }

    @Operation(summary = "Delete Consultant Partner Application")
    @DeleteMapping("/{id}")
    public SharkdomApiResponse<Void> delete(@PathVariable Long id) {

        log.info("Received request to delete ConsultantPartnerApplication with id: {}", id);
        service.delete(id);
        return new SharkdomApiResponse<>(true, "Deleted successfully", null);
    }

    @Operation(summary = "Get Consultant Partner Application by ID")
    @GetMapping("/{id}")
    public SharkdomApiResponse<ConsultantPartnerApplicationDTO> getById(@PathVariable Long id) {

        log.info("Received request to fetch ConsultantPartnerApplication with id: {}", id);
        return new SharkdomApiResponse<>(true, "Fetched successfully", service.getById(id));
    }

    @Operation(summary = "Get All Consultant Partner Applications with Pagination")
    @GetMapping
    public SharkdomApiResponse<SharkdomPaginatedResponse<ConsultantPartnerApplicationDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Received request to fetch ConsultantPartnerApplications page: {}, size: {}", page, size);
        return new SharkdomApiResponse<>(true, "Fetched successfully", service.getAll(page, size));
    }
}

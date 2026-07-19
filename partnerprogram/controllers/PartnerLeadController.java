package com.sharkdom.partnerprogram.controllers;

import com.sharkdom.partnerprogram.dtos.PartnerLeadDTO;
import com.sharkdom.partnerprogram.service.PartnerLeadService;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.SharkdomPaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partner-leads")
@RequiredArgsConstructor
public class PartnerLeadController {

    private final PartnerLeadService service;

    @Operation(summary = "Create Partner Lead")
    @PostMapping
    public SharkdomApiResponse<PartnerLeadDTO> create(
            @RequestBody PartnerLeadDTO dto
    ) {
        return new SharkdomApiResponse<>(
                true,
                "Lead created successfully",
                service.create(dto)
        );
    }

    @Operation(summary = "Update Partner Lead")
    @PutMapping("/{id}")
    public SharkdomApiResponse<PartnerLeadDTO> update(
            @PathVariable Long id,
            @RequestBody PartnerLeadDTO dto
    ) {
        return new SharkdomApiResponse<>(
                true,
                "Lead updated successfully",
                service.update(id, dto)
        );
    }

    @Operation(summary = "Delete Partner Lead")
    @DeleteMapping("/{id}")
    public SharkdomApiResponse<Void> delete(
            @PathVariable Long id
    ) {
        service.delete(id);

        return new SharkdomApiResponse<>(
                true,
                "Lead deleted successfully",
                null
        );
    }

    @Operation(summary = "Get Lead By Id")
    @GetMapping("/{id}")
    public SharkdomApiResponse<PartnerLeadDTO> getById(
            @PathVariable Long id
    ) {
        return new SharkdomApiResponse<>(
                true,
                "Lead fetched successfully",
                service.getById(id)
        );
    }

    @Operation(summary = "Get All Leads By UserId")
    @GetMapping
    public SharkdomApiResponse<SharkdomPaginatedResponse<PartnerLeadDTO>> getAll(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return new SharkdomApiResponse<>(
                true,
                "Leads fetched successfully",
                service.getAll(userId, page, size)
        );
    }
}

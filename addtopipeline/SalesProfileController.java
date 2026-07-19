package com.sharkdom.partnerattribution.addtopipeline;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales-profile")
@RequiredArgsConstructor
public class SalesProfileController {

    private final SalesProfileService salesProfileService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalesProfileResponseDto createSalesProfile(
            @Valid @RequestBody SalesProfileRequestDto requestDto) {

        return salesProfileService.createSalesProfile(requestDto);
    }

    @PutMapping("/{id}")
    public SalesProfileResponseDto updateSalesProfile(
            @PathVariable Long id,
            @Valid @RequestBody SalesProfileRequestDto requestDto) {

        return salesProfileService.updateSalesProfile(id, requestDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSalesProfile(@PathVariable Long id) {

        salesProfileService.deleteSalesProfile(id);
    }

    @GetMapping("/org/{orgId}")
    public List<SalesProfileResponseDto> getAllByOrgId(
            @PathVariable Long orgId) {

        return salesProfileService.getAllByOrgId(orgId);
    }
}
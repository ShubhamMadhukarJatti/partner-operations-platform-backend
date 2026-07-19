package com.sharkdom.partnerattribution.addtopipeline;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partner-deals")
@RequiredArgsConstructor
public class PartnerDealController {

    private final PartnerDealService partnerDealService;
    private final HubspotDealService hubspotDealService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartnerDealResponseDto createDeal(
            @Valid @RequestBody
            PartnerDealRequestDto requestDto) {

        return partnerDealService.createDeal(requestDto);
    }

    @PutMapping("/{id}")
    public PartnerDealResponseDto updateDeal(
            @PathVariable Long id,
            @Valid @RequestBody
            PartnerDealRequestDto requestDto) {

        return partnerDealService.updateDeal(
                id,
                requestDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDeal(@PathVariable Long id) {

        partnerDealService.deleteDeal(id);
    }

    @GetMapping("/{dealId}")
    public PartnerDealResponseDto getDealByDealId(
            @PathVariable String dealId) {

        return partnerDealService.getDealByDealId(dealId);
    }

    @PostMapping("/createDealHubspot")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateHubspotDealResponseDto createDealHubspot(
            @Valid @RequestBody
            CreateHubspotDealRequestDto requestDto) {

        return hubspotDealService
                .createDealHubspot(requestDto);
    }
}

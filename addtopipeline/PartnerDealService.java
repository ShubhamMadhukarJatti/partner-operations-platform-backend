package com.sharkdom.partnerattribution.addtopipeline;


public interface PartnerDealService {

    PartnerDealResponseDto createDeal(PartnerDealRequestDto requestDto);

    PartnerDealResponseDto updateDeal(Long id,
                                      PartnerDealRequestDto requestDto);

    void deleteDeal(Long id);

    PartnerDealResponseDto getDealByDealId(String dealId);
}
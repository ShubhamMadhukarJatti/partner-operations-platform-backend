package com.sharkdom.partnerattribution.addtopipeline;


import java.util.List;

public interface SalesProfileService {

    SalesProfileResponseDto createSalesProfile(SalesProfileRequestDto requestDto);

    SalesProfileResponseDto updateSalesProfile(Long id,
                                               SalesProfileRequestDto requestDto);

    void deleteSalesProfile(Long id);

    List<SalesProfileResponseDto> getAllByOrgId(Long orgId);
}
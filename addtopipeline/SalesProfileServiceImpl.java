package com.sharkdom.partnerattribution.addtopipeline;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.partnerattribution.entities.SalesProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesProfileServiceImpl implements SalesProfileService {

    private final SalesProfileRepository salesProfileRepository;

    @Override
    public SalesProfileResponseDto createSalesProfile(SalesProfileRequestDto requestDto) {

        SalesProfile salesProfile = new SalesProfile();

        mapToEntity(salesProfile, requestDto);

        SalesProfile savedProfile = salesProfileRepository.save(salesProfile);

        return mapToResponse(savedProfile);
    }

    @Override
    public SalesProfileResponseDto updateSalesProfile(Long id,
                                                      SalesProfileRequestDto requestDto) {

        SalesProfile salesProfile = salesProfileRepository.findById(id)
                .orElseThrow(() -> new ServiceException(
                        ErrorMessages.NOT_FOUND));

        mapToEntity(salesProfile, requestDto);

        SalesProfile updatedProfile = salesProfileRepository.save(salesProfile);

        return mapToResponse(updatedProfile);
    }

    @Override
    public void deleteSalesProfile(Long id) {

        SalesProfile salesProfile = salesProfileRepository.findById(id)
                .orElseThrow(() -> new ServiceException(
                        ErrorMessages.NOT_FOUND));

        salesProfileRepository.delete(salesProfile);
    }

    @Override
    public List<SalesProfileResponseDto> getAllByOrgId(Long orgId) {

        List<SalesProfile> salesProfiles =
                salesProfileRepository.findAllByOrgId(orgId);

        return salesProfiles.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void mapToEntity(SalesProfile salesProfile,
                             SalesProfileRequestDto requestDto) {

        salesProfile.setName(requestDto.getName());
        salesProfile.setRole(requestDto.getRole());
        salesProfile.setActiveDeals(requestDto.getActiveDeals());
        salesProfile.setWinRate(requestDto.getWinRate());
        salesProfile.setAvgCycleDays(requestDto.getAvgCycleDays());
        salesProfile.setTerritory(requestDto.getTerritory());
        salesProfile.setTerritoryMatched(requestDto.getTerritoryMatched());
        salesProfile.setOrgId(requestDto.getOrgId());
    }

    private SalesProfileResponseDto mapToResponse(SalesProfile salesProfile) {

        return SalesProfileResponseDto.builder()
                .id(salesProfile.getId())
                .name(salesProfile.getName())
                .role(salesProfile.getRole())
                .activeDeals(salesProfile.getActiveDeals())
                .winRate(salesProfile.getWinRate())
                .avgCycleDays(salesProfile.getAvgCycleDays())
                .territory(salesProfile.getTerritory())
                .territoryMatched(salesProfile.getTerritoryMatched())
                .orgId(salesProfile.getOrgId())
                .build();
    }
}
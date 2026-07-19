package com.sharkdom.agenticai.service;

import com.sharkdom.agenticai.entity.PartnerCompanyProfile;
import com.sharkdom.agenticai.entity.PartnerShipTeam;
import com.sharkdom.agenticai.model.PartnerCompanyProfileRequest;
import com.sharkdom.agenticai.model.PartnerCompanyProfileResponse;
import com.sharkdom.agenticai.model.PartnerShipTeamResponse;
import com.sharkdom.agenticai.repository.PartnerCompanyProfileRepository;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerCompanyProfileService {

    private final PartnerCompanyProfileRepository profileRepository;

    @Transactional
    public PartnerCompanyProfileResponse create(PartnerCompanyProfileRequest request) {

        log.info("Creating PartnerCompanyProfile for company: {}", request.getCompanyName());

        // Check if company already exists
        if (profileRepository.existsByCompanyNameIgnoreCase(request.getCompanyName())) {
            throw new ServiceException(ErrorMessages.SH198, request.getCompanyName());
        }

        PartnerCompanyProfile profile = new PartnerCompanyProfile();
        profile.setCompanyName(request.getCompanyName());
        profile.setAvgPartnerSourceRevenue(request.getAvgPartnerSourceRevenue());
        profile.setPartnerShipTeamSize(request.getPartnerShipTeamSize());
        profile.setSubsectors(request.getSubsectors());
        profile.setCompliances(request.getCompliances());
        profile.setDescription(request.getDescription());
        profile.setAbout(request.getAbout());
        profile.setWebsite(request.getWebsite());
        profile.setPartnerRange(request.getPartnerRange());

        if (request.getPartnerShipTeam() != null) {
            profile.setPartnerShipTeam(
                    request.getPartnerShipTeam().stream().map(teamReq -> {
                        PartnerShipTeam team = new PartnerShipTeam();
                        team.setName(teamReq.getName());
                        team.setEmail(teamReq.getEmail());
                        team.setLinkedin(teamReq.getLinkedin());
                        team.setTitle(teamReq.getTitle());
                        team.setPartnerCompanyProfile(profile);
                        return team;
                    }).collect(Collectors.toList())
            );
        }

        PartnerCompanyProfile saved = profileRepository.save(profile);

        log.info("PartnerCompanyProfile created successfully with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    public Page<PartnerCompanyProfileResponse> getAll(int page, int size) {

        log.info("Fetching PartnerCompanyProfiles page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return profileRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public PartnerCompanyProfileResponse getById(Long id) {

        log.info("Fetching PartnerCompanyProfile with id: {}", id);

        PartnerCompanyProfile profile = profileRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("PartnerCompanyProfile not found with id: {}", id);
                    return new ServiceException(ErrorMessages.NOT_FOUND);
                });

        log.info("PartnerCompanyProfile found with id: {}", id);

        return mapToResponse(profile);
    }

    @Transactional
    public void deleteById(Long id) {

        log.info("Attempting to delete PartnerCompanyProfile with id: {}", id);

        PartnerCompanyProfile profile = profileRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Delete failed. PartnerCompanyProfile not found with id: {}", id);
                    return new ServiceException(ErrorMessages.NOT_FOUND);
                });

        profileRepository.delete(profile);

        log.info("PartnerCompanyProfile deleted successfully with id: {}", id);
    }

    private PartnerCompanyProfileResponse mapToResponse(PartnerCompanyProfile profile) {

        return PartnerCompanyProfileResponse.builder()
                .id(profile.getId())
                .companyName(profile.getCompanyName())
                .avgPartnerSourceRevenue(profile.getAvgPartnerSourceRevenue())
                .partnerShipTeamSize(profile.getPartnerShipTeamSize())
                .subsectors(profile.getSubsectors())
                .description(profile.getDescription())
                .about(profile.getAbout())
                .compliances(profile.getCompliances())
                .website(profile.getWebsite())

                .partnerRange(profile.getPartnerRange())
                .partnerShipTeam(
                        profile.getPartnerShipTeam() == null ? null :
                                profile.getPartnerShipTeam().stream()
                                        .map(team -> PartnerShipTeamResponse.builder()
                                                .name(team.getName())
                                                .linkedin(team.getLinkedin())
                                                .title(team.getTitle())
                                                .email(team.getEmail())
                                                .build())
                                        .collect(Collectors.toList())
                )
                .build();
    }
}
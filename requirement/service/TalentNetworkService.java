package com.sharkdom.requirement.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.requirement.entity.TalentNetwork;
import com.sharkdom.requirement.repository.TalentNetworkRepository;
import com.sharkdom.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TalentNetworkService {

    private final TalentNetworkRepository talentNetworkRepository;
    private final EmailService emailService;

    public TalentNetwork createTalentNetwork(TalentNetwork request) throws Exception {

        if (request == null) {
            throw new ServiceException(ErrorMessages.SH05);
        }

        // Required field validations
        if (!StringUtils.hasText(request.getCompanyName())) {
            throw new ServiceException(ErrorMessages.SH189);
        }

        if (!StringUtils.hasText(request.getContactEmail())) {
            throw new ServiceException(ErrorMessages.SH04);
        }

        if (!StringUtils.hasText(request.getJobTitle())) {
            throw new ServiceException(ErrorMessages.SH189);
        }

        // Duplicate email check
        if (talentNetworkRepository.existsByContactEmailIgnoreCase(request.getContactEmail())) {
            throw new ServiceException(ErrorMessages.SH102, request.getContactEmail());
        }

        // Trim & normalize
        request.setCompanyName(request.getCompanyName().trim());
        request.setContactEmail(request.getContactEmail().trim().toLowerCase());
        request.setJobTitle(request.getJobTitle().trim());

        if (request.getWebsiteUrl() != null) {
            request.setWebsiteUrl(request.getWebsiteUrl().trim());
        }

        if (request.getLinkedinUrl() != null) {
            request.setLinkedinUrl(request.getLinkedinUrl().trim());
        }


        var talentNetwork = talentNetworkRepository.save(request);

        if (talentNetwork != null) {
            emailService.sendTalentNetworkCreatedEmail(
                    "TALENT_NETWORK_CREATED_TO_CSM",
                    talentNetwork,"deepak.v@sharkdom.com"
            );

            emailService.sendTemplateWithUserName("TALENT_NETWORK_CREATED", talentNetwork.getContactEmail(),talentNetwork.getCompanyName());
        }


        return talentNetwork;
    }

    public List<TalentNetwork> getAllTalentNetworks() {
        return talentNetworkRepository.findAll();
    }




}

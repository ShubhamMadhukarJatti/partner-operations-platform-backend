package com.sharkdom.requirement.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.requirement.entity.CommunityOptIn;
import com.sharkdom.requirement.repository.CommunityOptInRepository;
import com.sharkdom.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityOptInService {

    private final CommunityOptInRepository repository;
    private final EmailService emailService;


    @Transactional
    public CommunityOptIn createOptIn(CommunityOptIn request) throws Exception {

        if (request == null) {
            throw new ServiceException(ErrorMessages.SH05);
        }

        if (!StringUtils.hasText(request.getApplicantName())
                || !StringUtils.hasText(request.getContactEmail())
                || !StringUtils.hasText(request.getLinkedinUrl())
                || !StringUtils.hasText(request.getJobTitle())
                || !StringUtils.hasText(request.getPreferredLocation())) {
            throw new ServiceException(ErrorMessages.SH106);
        }

        if (repository.existsByContactEmailIgnoreCase(request.getContactEmail())) {
            throw new ServiceException(ErrorMessages.SH102, request.getContactEmail());
        }

        // Normalize
        request.setApplicantName(request.getApplicantName().trim());
        request.setContactEmail(request.getContactEmail().trim().toLowerCase());
        request.setLinkedinUrl(request.getLinkedinUrl().trim());
        request.setJobTitle(request.getJobTitle().trim());
        request.setPreferredLocation(request.getPreferredLocation().trim());

        var communityOptIn = repository.save(request);
        if (communityOptIn!=null)
        {
            emailService.sendTemplateWithUserName("COMMUNITY_OPT_IN_SUBMITTED",communityOptIn.getContactEmail(),communityOptIn.getApplicantName());
            emailService.sendCommunityOptInEmailToADMIN("COMMUNITY_OPT_IN_SUBMITTED_ADMIN",communityOptIn,"deepak.v@sharkdom.com");
            emailService.sendCommunityOptInEmailToUSER("COMMUNITY_OPT_IN_SUBMITTED_ADMIN",communityOptIn,communityOptIn.getContactEmail());
        }
        return communityOptIn;
    }


    public List<CommunityOptIn> getAllOptIns() {
        return repository.findAll();
    }
}

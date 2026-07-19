package com.sharkdom.service.credits;


import com.sharkdom.entity.credits.Credits;
import com.sharkdom.model.credits.CreditsModel;
import com.sharkdom.repository.credits.CreditsRepository;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CreditsService {

    private final CreditsRepository creditsRepository;

    public CreditsService(CreditsRepository creditsRepository) {
        this.creditsRepository = creditsRepository;

    }

    public ResponseEntity<Credits> deductCredits(CreditsModel creditsModel) {
        creditsModel.setOrganizationId(Util.getOrgIdFromToken());
        Credits credits = creditsRepository.findByOrganizationId(creditsModel.getOrganizationId());

        int playgroundCreditsLeft = credits.getPlaygroundLeft() - creditsModel.getPlaygroundCredits();
        int aiProposalCreditsLeft = credits.getAiProposalLeft() - creditsModel.getAiProposalCredits();
        credits.setPlaygroundLeft(playgroundCreditsLeft);
        credits.setAiProposalLeft(aiProposalCreditsLeft);
        return ResponseEntity.ok(creditsRepository.save(credits));
    }

    public ResponseEntity<Credits> getCredits() {
        var organizationId = Util.getOrgIdFromToken();
        Credits credits = creditsRepository.findByOrganizationId(organizationId);
        return ResponseEntity.ok(credits);

    }

}

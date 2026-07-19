package com.sharkdom.service.credits;

import com.sharkdom.entity.credits.Credit;
import com.sharkdom.repository.credits.CreditRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreditService {

    private final CreditRepository creditRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private boolean executed = false;

    public boolean hasCredits( Long orgId) {
        boolean result = creditRepository.findByOrgId(orgId)
                .map(c -> c.getCredits() > 0)
                .orElse(true); // if no record exists, assume 2 credits available

        log.debug("Checking credits for orgId={}, hasCredits={}", orgId, result);
        return result;
    }


    @Transactional
    public String consumeCredit( Long orgId) {
        Credit credit = creditRepository.findByOrgId(orgId).orElse(
                Credit.builder()
                        .orgId(orgId)
                        .credits(2) // default credits
                        .build()
        );

        log.info("Attempting to consume credit for orgId={}, currentCredits={}",
                orgId, credit.getCredits());

        if (credit.getCredits() <= 0) {
            log.warn("Credit limit reached for orgId={}. No credits remaining.", orgId);
            return "Your credits are finished. Please upgrade to continue searching.";
        }

        credit.setCredits(credit.getCredits() - 1);
        creditRepository.save(credit);

        log.info("Credit consumed for orgId={}, remainingCredits={}",
                orgId, credit.getCredits());

        return "Search allowed. Remaining credits: " + credit.getCredits();
    }


    @Transactional
    public void addCreditIfNotExists(Long orgId, int credits) {
        boolean exists = creditRepository.findByOrgId(orgId).isPresent();

        if (exists) {
            return; // already exists → do nothing
        }

        Credit credit = Credit.builder()
                .orgId(orgId)
                .credits(credits)
                .build();

        creditRepository.save(credit);
    }
}

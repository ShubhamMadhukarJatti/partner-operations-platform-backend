package com.sharkdom.profilesection.jobs;

import com.sharkdom.agenticai.model.*;
import com.sharkdom.agenticai.service.ReviewFetchService;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.repository.organization.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrustpilotReviewSyncService {

    private final OrganizationRepository organizationRepository;
    private final ReviewFetchService reviewFetchService;

    public void syncReviews() {

        List<Organization> organizations =
                organizationRepository.findByWebsiteIsNotNull();

        log.info("Starting Trustpilot sync. totalOrg={}", organizations.size());

        for (Organization org : organizations) {

            try {

                ReviewFetchRequest request = new ReviewFetchRequest();
                request.setOrgName(org.getName());
                request.setOrgWebURL(org.getWebsite());

                ReviewFetchResponse response =
                        reviewFetchService.fetchReviews(request);

                if (Boolean.TRUE.equals(response.getSuccess())
                        && response.getData() != null
                        && response.getData().getTrustpilot() != null
                        && Boolean.TRUE.equals(response.getData().getTrustpilot().getFound())) {

                    TrustpilotReview review =
                            response.getData().getTrustpilot();

                    org.setTrustpilotRating(review.getRating());
                    org.setTrustpilotTotalReviews(review.getTotalReviews());

                    organizationRepository.save(org);

                    log.info("Trustpilot updated. org={} rating={} reviews={}",
                            org.getName(),
                            review.getRating(),
                            review.getTotalReviews());

                }

            } catch (Exception ex) {

                log.error("Failed to fetch review for org={}",
                        org.getName(), ex);
            }

        }

        log.info("Trustpilot sync completed.");
    }
}
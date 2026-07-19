package com.sharkdom.partnerattribution.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.partnerattribution.enums.DealStageAlignment;
import com.sharkdom.partnerattribution.enums.EngagementRecency;
import com.sharkdom.partnerattribution.enums.RelationshipDepth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OpportunityScoreService {

    public int calculateScore(
            DealStageAlignment stageAlignment,
            EngagementRecency engagementRecency,
            boolean companySizeMatch,
            boolean industryMatch,
            boolean geographyMatch
    ) {

        log.info("Starting opportunity score calculation");

        try {

            validateInputs(stageAlignment, engagementRecency);

            int score = 0;

            int stageScore = calculateStageAlignment(stageAlignment);
            int engagementScore = calculateEngagementRecency(engagementRecency);
            int icpScore = calculateICPFit(companySizeMatch, industryMatch, geographyMatch);

            score = stageScore + engagementScore + icpScore;

            log.debug("Opportunity score breakdown -> stage: {}, engagement: {}, icp: {}",
                    stageScore,engagementScore, icpScore);

            log.info("Final opportunity score calculated: {}", score);

            return score;

        } catch (ServiceException ex) {

            log.error("Validation error during opportunity score calculation: {}", ex.getMessage());
            throw ex;

        } catch (Exception ex) {

            log.error("Unexpected error while calculating opportunity score", ex);
            throw new ServiceException(ErrorMessages.SH160, ex.getMessage());
        }
    }

    private void validateInputs(
            DealStageAlignment stageAlignment,
            EngagementRecency engagementRecency) {

        if (stageAlignment == null) {
            log.error("DealStageAlignment is null");
            throw new ServiceException(ErrorMessages.SH106);
        }

        if (engagementRecency == null) {
            log.error("EngagementRecency is null");
            throw new ServiceException(ErrorMessages.SH106);
        }
    }

    private int calculateStageAlignment(DealStageAlignment alignment) {

        log.debug("Calculating stage alignment score for {}", alignment);

        return switch (alignment) {

            case BOTH_ACTIVE_PIPELINE -> 30;

            case ONE_PIPELINE_ONE_PROSPECT -> 20;

            case ONE_CLOSED_CUSTOMER -> 25;

            case NEITHER_PIPELINE -> 5;
        };
    }


    private int calculateEngagementRecency(EngagementRecency recency) {

        log.debug("Calculating engagement recency score for {}", recency);

        return switch (recency) {

            case LESS_THAN_7_DAYS -> 25;

            case LESS_THAN_30_DAYS -> 18;

            case LESS_THAN_60_DAYS -> 10;

            case LESS_THAN_90_DAYS -> 5;

            case NONE -> 0;
        };
    }

    private int calculateICPFit(
            boolean companySizeMatch,
            boolean industryMatch,
            boolean geographyMatch) {

        log.debug("Calculating ICP fit score");

        int score = 0;

        if (companySizeMatch) {
            score += 8;
        }

        if (industryMatch) {
            score += 6;
        }

        if (geographyMatch) {
            score += 2;
        }

        log.debug("ICP fit score calculated: {}", score);

        return score;
    }
}
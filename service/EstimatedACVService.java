package com.sharkdom.partnerattribution.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.partnerattribution.enums.CompanySize;
import com.sharkdom.partnerattribution.enums.Geography;
import com.sharkdom.partnerattribution.enums.PlanComplexity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EstimatedACVService {

    public double calculateEstimatedACV(
            CompanySize companySize,
            PlanComplexity planComplexity,
            Geography geography
    ) {

        log.info("Starting Estimated ACV calculation");

        try {

            validateInputs(companySize, planComplexity, geography);

            double baseline = getCompanySizeBaseline(companySize);
            double planMultiplier = getPlanMultiplier(planComplexity);
            double geographyMultiplier = getGeographyMultiplier(geography);

            double estimatedACV = baseline * planMultiplier * geographyMultiplier;

            log.debug("ACV calculation breakdown -> baseline: {}, planMultiplier: {}, geographyMultiplier: {}",
                    baseline, planMultiplier, geographyMultiplier);

            log.info("Estimated ACV calculated: {}", estimatedACV);

            return estimatedACV;

        } catch (ServiceException ex) {

            log.error("Validation error during ACV calculation: {}", ex.getMessage());
            throw ex;

        } catch (Exception ex) {

            log.error("Unexpected error while calculating ACV", ex);
            throw new ServiceException(ErrorMessages.SH160, ex.getMessage());
        }
    }

    private void validateInputs(
            CompanySize companySize,
            PlanComplexity planComplexity,
            Geography geography
    ) {

        if (companySize == null) {
            log.error("CompanySize is null");
            throw new ServiceException(ErrorMessages.SH106);
        }

        if (planComplexity == null) {
            log.error("PlanComplexity is null");
            throw new ServiceException(ErrorMessages.SH106);
        }

        if (geography == null) {
            log.error("Geography is null");
            throw new ServiceException(ErrorMessages.SH106);
        }
    }

    private double getCompanySizeBaseline(CompanySize companySize) {

        log.debug("Fetching company size baseline for {}", companySize);

        return switch (companySize) {

            case SIZE_11_50 -> 12000;

            case SIZE_51_200 -> 25000;

            case SIZE_201_500 -> 55000;

            case SIZE_500_PLUS -> 100000;
        };
    }

    private double getPlanMultiplier(PlanComplexity complexity) {

        log.debug("Fetching plan multiplier for {}", complexity);

        return switch (complexity) {

            case ADDONS_1_2 -> 1.0;

            case ADDONS_3_4 -> 1.4;

            case ADDONS_5_6 -> 1.8;

            case FULL_PLATFORM -> 2.2;
        };
    }

    private double getGeographyMultiplier(Geography geography) {

        log.debug("Fetching geography multiplier for {}", geography);

        return switch (geography) {

            case US -> 1.3;

            case EUROPE -> 1.2;

            case APAC -> 1.1;

            case OTHER -> 1.0;
        };
    }
}
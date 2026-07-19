package com.sharkdom.partnerattribution.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class m {

////    private final ClosedWonDealRepository closedWonDealRepository;
//
//    /**
//     * V1 ACV Estimation Logic
//     * -------------------------------------
//     * Approach A:
//     * Mean ACV by Segment
//     *
//     * estimated_acv =
//     * average ACV of closed-won deals
//     * where:
//     * company_size = target_size
//     * industry = target_industry
//     * geography = target_geo
//     */
//    public EstimatedAcvResult calculateEstimatedAcv(String companySize,
//                                                    String industry,
//                                                    String geography) {
//
//        log.info("""
//                Starting estimated ACV calculation.
//                companySize={}
//                industry={}
//                geography={}
//                """,
//                companySize,
//                industry,
//                geography
//        );
//
//        /*
//         * Fetch matching closed-won deals
//         */
//        List<ClosedWonDeal> matchingDeals =
//                closedWonDealRepository.findClosedWonDeals(
//                        companySize,
//                        industry,
//                        geography
//                );
//
//        log.info("""
//                Matching closed-won deals fetched.
//                totalDeals={}
//                """,
//                matchingDeals.size()
//        );
//
//        /*
//         * No matching deals found
//         */
//        if (matchingDeals.isEmpty()) {
//
//            log.warn("""
//                    No matching closed-won deals found.
//                    Returning UNKNOWN estimation.
//                    """);
//
//            return EstimatedAcvResult.builder()
//                    .estimatedAcv(BigDecimal.ZERO)
//                    .confidenceLevel("LOW")
//                    .sampleSize(0)
//                    .calculationMethod("SEGMENT_MEAN")
//                    .message("Insufficient historical deal data")
//                    .build();
//        }
//
//        /*
//         * Calculate average ACV
//         */
//        OptionalDouble averageAcvOptional =
//                matchingDeals.stream()
//                        .mapToDouble(deal -> deal.getAcv().doubleValue())
//                        .average();
//
//        double averageAcv =
//                averageAcvOptional.orElse(0.0);
//
//        BigDecimal estimatedAcv =
//                BigDecimal.valueOf(averageAcv)
//                        .setScale(2, RoundingMode.HALF_UP);
//
//        /*
//         * Confidence calculation based on sample size
//         */
//        String confidenceLevel =
//                determineConfidenceLevel(matchingDeals.size());
//
//        log.info("""
//                Estimated ACV calculated successfully.
//                estimatedAcv={}
//                confidenceLevel={}
//                sampleSize={}
//                """,
//                estimatedAcv,
//                confidenceLevel,
//                matchingDeals.size()
//        );
//
//        return EstimatedAcvResult.builder()
//                .estimatedAcv(estimatedAcv)
//                .confidenceLevel(confidenceLevel)
//                .sampleSize(matchingDeals.size())
//                .calculationMethod("SEGMENT_MEAN")
//                .message("Estimated using historical segment average")
//                .build();
//    }
//
//    /**
//     * Confidence Logic
//     */
//    private String determineConfidenceLevel(int sampleSize) {
//
//        log.debug("""
//                Determining confidence level.
//                sampleSize={}
//                """, sampleSize);
//
//        if (sampleSize >= 100) {
//            return "HIGH";
//        }
//
//        if (sampleSize >= 50) {
//            return "MEDIUM";
//        }
//
//        if (sampleSize >= 10) {
//            return "LOW";
//        }
//
//        return "VERY_LOW";
//    }
}

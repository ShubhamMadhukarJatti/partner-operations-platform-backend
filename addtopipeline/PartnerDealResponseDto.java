package com.sharkdom.partnerattribution.addtopipeline;

import com.sharkdom.constants.partnerDeals.DealStage;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class PartnerDealResponseDto {

    private Long id;

    private String dealName;

    private String dealId;

    private String salesTeamMemberId;

    private Long orgId;

    private Long partnerOrgId;

    private String accountName;

    private DealPipelineType pipelineType;

    private DealStage dealStage;

    private Double estimatedAcv;

    private LocalDate targetCloseDate;

    private DealPriorityLevel priorityLevel;

    private Integer opportunityScore;

    private String aeNotes;

    private List<String> dealTags;
}
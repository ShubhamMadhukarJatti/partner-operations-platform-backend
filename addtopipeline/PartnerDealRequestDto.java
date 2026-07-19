package com.sharkdom.partnerattribution.addtopipeline;

import com.sharkdom.constants.partnerDeals.DealStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PartnerDealRequestDto {

    @NotBlank(message = "Deal Id is required")
    private String dealId;

    private String salesTeamMemberId;

    @NotNull(message = "Organization Id is required")
    private Long orgId;

    private Long partnerOrgId;

    private String accountName;

    @NotNull(message = "Pipeline type is required")
    private DealPipelineType pipelineType;

    @NotNull(message = "Deal stage is required")
    private DealStage dealStage;

    private Double estimatedAcv;

    private LocalDate targetCloseDate;

    @NotNull(message = "Priority level is required")
    private DealPriorityLevel priorityLevel;

    private Integer opportunityScore;

    private String aeNotes;

    private List<String> dealTags;

    private String dealName;
}
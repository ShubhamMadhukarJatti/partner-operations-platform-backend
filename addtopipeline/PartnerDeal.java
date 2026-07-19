package com.sharkdom.partnerattribution.addtopipeline;

import com.sharkdom.constants.partnerDeals.DealStage;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "partner_deals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerDeal extends BaseEntity {

    /**
     * External CRM Deal Id
     * Example:
     * HUBSPOT_DEAL_123
     * SALESFORCE_456
     */
    @Column(name = "deal_id", nullable = false, unique = true)
    private String dealId;

    /**
     * CRM Sales Team Member Id
     * Example:
     * HubSpot Owner Id
     * Salesforce User Id
     */
    @Column(name = "sales_team_member_id")
    private String salesTeamMemberId;

    /**
     * Organization Id
     */
    @Column(name = "org_id", nullable = false)
    private Long orgId;

    /**
     * Partner Organization Id
     */
    @Column(name = "partner_org_id")
    private Long partnerOrgId;

    /**
     * Company / Account Name
     */
    @Column(name = "account_name")
    private String accountName;

    /**
     * Deal Pipeline Type
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "pipeline_type")
    private DealPipelineType pipelineType;

    /**
     * Current Stage
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "deal_stage")
    private DealStage dealStage;

    /**
     * Estimated ACV
     */
    @Column(name = "estimated_acv")
    private Double estimatedAcv;

    /**
     * Target Close Date
     */
    @Column(name = "target_close_date")
    private LocalDate targetCloseDate;

    /**
     * Deal Priority
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority_level")
    private DealPriorityLevel priorityLevel;

    /**
     * Opportunity Score
     */
    @Column(name = "opportunity_score")
    private Integer opportunityScore;

    /**
     * AE Notes
     */
    @Column(name = "ae_notes", columnDefinition = "TEXT")
    private String aeNotes;

    /**
     * Deal Tags
     */
    @ElementCollection
    @CollectionTable(
            name = "partner_deal_tags",
            joinColumns = @JoinColumn(name = "partner_deal_id")
    )
    @Column(name = "tag")
    private List<String> dealTags;

    private String dealName;
}

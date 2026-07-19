package com.sharkdom.deals.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "deals_details")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealsEntity extends BaseEntity {
    @Column(name = "deal_id", nullable = false, unique = true)
    private String dealId;
    @Column(name = "org_id", nullable = false)
    private Long organizationId;

    @Column(name = "offer_detail", nullable = false)
    private String offerDetail;

    @Column(name = "commission", nullable = false)
    private Integer commission;

    @ElementCollection
    @CollectionTable(name = "restricted_sectors", joinColumns = @JoinColumn(name = "deal_id"))
    @Column(name = "sector")
    private String[] restrictedSectors;

    @ElementCollection
    @CollectionTable(name = "channel_allowed", joinColumns = @JoinColumn(name = "deal_id"))
    @Column(name = "channel")
    private String[] channelAllowed;

    @Column(name = "quota_remaining", nullable = false)
    private String quotaRemaining;

    @Column(name = "geography", nullable = false)
    private String geography;

    @Column(name = "approval_required", nullable = false)
    private boolean approvalRequired;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "page_url")
    private String pageURL;

}

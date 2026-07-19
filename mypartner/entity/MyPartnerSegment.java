package com.sharkdom.mypartner.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Data
@Table(name = "my_partner_segment")
public class MyPartnerSegment extends BaseEntity {

    @JsonProperty("color")
    @Column(nullable = false)
    private String color;

    @JsonProperty("segment_name")
    @Column(name = "segment_name", nullable = false)
    private String segmentName;

    @JsonProperty("organization_id")
    @Column(name = "organization_id")
    private long organizationId;

    @JsonProperty("min_deals")
    @Column(name = "min_deals", nullable = false)
    private Integer minDeals = 0;

    @JsonProperty("max_deals")
    @Column(name = "max_deals", nullable = false)
    private Integer maxDeals = 3;

    @JsonProperty("active_co_marketing_campaign")
    @Column(name = "active_co_marketing_campaign", nullable = false)
    private Boolean activeCoMarketingCampaign = true;

    @JsonProperty("access")
    private String access = "Full Access";

    // Ensure default values before saving
    @PrePersist
    public void setDefaults() {
        if (minDeals == null) minDeals = 0;
        if (maxDeals == null) maxDeals = 0;
        if (activeCoMarketingCampaign == null) activeCoMarketingCampaign = true;
        if (access == null || access.isBlank()) access = "Full Access";
    }
}


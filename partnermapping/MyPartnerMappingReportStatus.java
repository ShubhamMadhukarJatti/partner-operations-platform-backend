package com.sharkdom.entity.partnermapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity @Data
@Table(name="t_my_partner_mapping_report_status")
public class MyPartnerMappingReportStatus extends BaseEntity {

    @JsonProperty("organization_id")
    @Column(name="organization_id", nullable = false)
    private Long organizationId;

    @JsonProperty("your_matrix")
    @Column(name="your_matrix", columnDefinition = "TEXT")
    private String yourMatrix;

    @JsonProperty("partner_matrix")
    @Column(name="partner_matrix", columnDefinition = "TEXT")
    private String partnerMatrix;

    @JsonProperty("overlap_count")
    @Column(name="overlap_count")
    private Integer overlapCount;

    @JsonProperty("partner_id")
    @Column(name="partner_id")
    private Long partnerId;

    @JsonProperty("report_count")
    @Column(name="report_count")
    private Long reportCount;

    @JsonProperty("user_id")
    @Column(name="user_id")
    private String userId;

}

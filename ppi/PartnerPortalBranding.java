package com.sharkdom.entity.ppi;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name="t_partner_portal_brandings")
public class PartnerPortalBranding extends BaseEntity {

    @Column(name="PROGRAM TITLE", columnDefinition = "TEXT")
    private String title;

    @Column(name="DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name="URL", columnDefinition = "TEXT")
    private String url;

    @Column(name= "ORGANIZATION_ID")
    private Long organizationId;

    @Column(name="ENABLED_REFERRAL_PROGRAM")
    private Boolean enabledReferralProgram;
}

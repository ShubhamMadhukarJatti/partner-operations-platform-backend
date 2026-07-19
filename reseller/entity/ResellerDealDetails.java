package com.sharkdom.reseller.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "t_reseller_deal_details")
public class ResellerDealDetails extends BaseEntity {

    @Column(name = "reseller_org_id")
    private Long resellerOrgId;

    @Column(name = "vendor_org_id")
    private Long vendorOrgId;

    @Column(name = "partner_name")
    private String partnerName;

    @Column(name="expected_release_time")
    private Long expectedReleaseTime;

    @Column(name="expected_release_date")
    private Date expectedReleaseDate;

    @Column(name="reseller_mode")
    private String resellerMode;

    @Column(name="product_plan_required")
    private String productPlanRequired;

    @Column(name="number_of_licences")
    private Long numberOfLicences;

    @Column(name="calculated_partner_tier")
    private String calculatedPartnerTier;

    @Column(name="billing_model")
    private String billingModel;

    @Column(name="actual_price")
    private Double actualPrice;

    @Column(name="buy_price")
    private Double buyPrice;

    @Enumerated(EnumType.STRING)
    private ResellerDealStag resellerDealStag;

    @Enumerated(EnumType.STRING)
    private ResellerDealStatus resellerDealStatus;

    @Enumerated(EnumType.STRING)
    private ResellerDealSource resellerDealSource;

    @Column(name="poc")
    private String poc;

    @Column(name="website")
    private String website;

    @Column(name="hubspot_deal_id")
    private String hubspotDealId;

    @Column(name = "lastUpdatedTimestamp")
    private Date lastUpdatedTimestamp;

    @Column(name = "last_activity")
    private String lastActivity;
}

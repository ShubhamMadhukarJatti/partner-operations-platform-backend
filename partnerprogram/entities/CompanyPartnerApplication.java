package com.sharkdom.partnerprogram.entities;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.partnerprogram.enums.ApplicationStatus;
import com.sharkdom.partnerprogram.enums.CompanySize;
import com.sharkdom.partnerprogram.enums.PartnerProgramMaturity;
import com.sharkdom.partnerprogram.enums.PartnerType;
import jakarta.persistence.*;
import lombok.*;


@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_company_partner_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyPartnerApplication extends BaseEntity {

    // Company Details
    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String companyWebsite;

    // Contact Details
    @Column(nullable = false)
    private String primaryContactName;

    @Column(nullable = false, unique = true)
    private String contactEmail;

    // Company Info
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanySize companySize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnerType partnerType;

    @Column(nullable = false, length = 2000)
    private String icpFitExplanation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnerProgramMaturity maturity;

    // Relationship
    @Column(nullable = false)
    private Boolean hasExistingRelationship;

    // Agreements
    @Column(nullable = false)
    private Boolean informationConfirmed;

    @Column(nullable = false)
    private Boolean agreedToTerms;

    // Optional: Status tracking
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Column(length = 2000)
    private String reviewComments;

    private Boolean isActive = true;

}

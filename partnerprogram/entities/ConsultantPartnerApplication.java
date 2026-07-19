package com.sharkdom.partnerprogram.entities;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.partnerprogram.enums.*;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "consultant_partner_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultantPartnerApplication extends BaseEntity {

    // Basic Info
    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String linkedinProfileUrl;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Country country;

    // Professional Info
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleDescription roleDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private B2BAdvisoryCount advisoryCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ARRRange arrRange;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ARRRange typicalClientArrRange;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartnerProgramStatus partnerProgramStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadSource leadSource;

    // Consent Flags
    @Column(nullable = false)
    private Boolean useDweepBot;

    @Column(nullable = false)
    private Boolean acceptCommissionTerms;

    @Column(nullable = false)
    private Boolean agreeToTerms;

}
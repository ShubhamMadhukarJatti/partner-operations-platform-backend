package com.sharkdom.partnerprogram.entities;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.partnerprogram.enums.GtmFocusType;
import com.sharkdom.partnerprogram.enums.PartnershipTier;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "partner_application"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerApplication extends BaseEntity {

    @NotBlank
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "userId")
    private String userId;

    @NotBlank
    @Email
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank
    @Column(name = "linkedin_profile_url", nullable = false, length = 500)
    private String linkedInProfileUrl;

    @Column(name = "company_name", length = 150)
    private String companyName;

    @Column(name = "geography", length = 100)
    private String geography;

    @Column(name = "companies_advised", columnDefinition = "TEXT")
    private String companiesAdvised;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "t_partner_gtm_focus",
            joinColumns = @JoinColumn(name = "partner_application_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "gtm_focus")
    private Set<GtmFocusType> primaryGtmFocus = new HashSet<>();

    @Column(name = "hear_about_program", length = 255)
    private String howDidYouHearAboutProgram;

    @Enumerated(EnumType.STRING)
    @Column(name = "partnership_tier", nullable = false)
    private PartnershipTier partnershipTier;

    @Size(max = 200)
    @Column(name = "network_description", length = 200)
    private String networkDescription;

    @Column(name = "refer_code")
    private String referCode;

    @Column(name = "total_earnings", precision = 19, scale = 2)
    private BigDecimal totalEarnings;

    // Pending commissions
    @Column(name = "pending_commission", precision = 19, scale = 2)
    private BigDecimal pendingCommission;

    // Next payout date
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "next_payout_date")
    private Date nextPayoutDate;
}

package com.sharkdom.partnerprogram.entities;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.partnerprogram.enums.LeadStatus;
import com.sharkdom.partnerprogram.enums.LeadTemperature;
import com.sharkdom.partnerprogram.enums.PartnershipTier;
import com.sharkdom.reseller.entity.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(
        name = "partner_leads",
        indexes = {
                @Index(name = "idx_partner_lead_email", columnList = "contact_email"),
                @Index(name = "idx_partner_lead_company", columnList = "company_name"),
                @Index(name = "idx_partner_lead_status", columnList = "lead_status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerLead extends BaseEntity {

    /**
     * Partner who submitted this lead
     */
    @Column(name = "userId", nullable = false)
    private String userId;

    /**
     * Company Information
     */
    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_website")
    private String companyWebsite;

    @Column(name = "industry")
    private String industry;

    @Column(name = "company_size")
    private String companySize;

    @Column(name = "geography")
    private String geography;

    @Column(name = "estimated_acv", precision = 19, scale = 2)
    private BigDecimal estimatedAcv;

    /**
     * Contact Information
     */
    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_title")
    private String contactTitle;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_linkedin")
    private String contactLinkedIn;

    @Column(name = "buying_intent_signal", length = 300)
    private String buyingIntentSignal;

    /**
     * Hot / Warm / Cold
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "lead_temperature")
    private LeadTemperature leadTemperature;

    /**
     * Champion / Referral
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "involvement_level")
    private PartnershipTier involvementLevel;

    @Column(name = "preferred_meeting_format")
    private String preferredMeetingFormat;

    @Column(name = "message_to_team", length = 500)
    private String messageToSharkdomTeam;

    /**
     * Computed/Stored
     */
    @Column(name = "estimated_commission", precision = 19, scale = 2)
    private BigDecimal estimatedCommission;

    /**
     * Commission display text from UI
     * Example: $2,250 - $3,000
     */
    @Column(name = "estimated_commission_display")
    private String estimatedCommissionDisplay;

    /**
     * Pipeline status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "lead_status")
    private LeadStatus leadStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "partnership_tier")
    private PartnershipTier partnershipTier;

    @Column(name = "rate")
    private BigDecimal rate;

    /**
     * Submitted date
     */
    @Column(name = "submitted_date")
    private Date submittedDate;

    /**
     * Payment Details
     */
    @Column(name = "payment_date")
    private Date paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "invoice_link")
    private String invoiceLink;

    /**
     * Internal assigned AE
     */
    @Column(name = "assigned_ae")
    private String assignedAe;

    /**
     * UI / Workflow Fields
     */

    @Column(name = "status_label")
    private String statusLabel;

    @Column(name = "action_label")
    private String actionLabel;

    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "can_resubmit")
    private Boolean canResubmit;

    @Column(name = "can_cancel")
    private Boolean canCancel;

    @Column(name = "can_view_commission")
    private Boolean canViewCommission;

    @Column(name = "can_view_progress")
    private Boolean canViewProgress;

    @Column(name = "can_view_details")
    private Boolean canViewDetails;

    /**
     * CRM / Hubspot Mapping
     */

    @Column(name = "crm_deal_id")
    private String crmDealId;

    @Column(name = "hubspot_deal_id")
    private String hubspotDealId;

}
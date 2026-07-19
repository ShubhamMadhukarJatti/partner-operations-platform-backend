package com.sharkdom.partnerprogram.dtos;

import com.sharkdom.partnerprogram.enums.LeadStatus;
import com.sharkdom.partnerprogram.enums.LeadTemperature;
import com.sharkdom.partnerprogram.enums.PartnershipTier;
import com.sharkdom.reseller.entity.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PartnerLeadDTO {

    private Long id;

    /**
     * Partner user id
     */
    private String userId;

    /**
     * Company Information
     */
    private String companyName;

    private String companyWebsite;

    private String industry;

    private String companySize;

    private String geography;

    private BigDecimal estimatedAcv;

    /**
     * Contact Information
     */
    private String contactName;

    private String contactTitle;

    private String contactEmail;

    private String contactLinkedIn;

    private String buyingIntentSignal;

    /**
     * Lead Temperature
     */
    private LeadTemperature leadTemperature;

    /**
     * Champion / Referral
     */
    private PartnershipTier involvementLevel;

    private String preferredMeetingFormat;

    private String messageToSharkdomTeam;

    /**
     * Commission details
     */
    private BigDecimal estimatedCommission;

    private BigDecimal rate;

    /**
     * Lead Pipeline Status
     */
    private LeadStatus leadStatus;

    /**
     * Partnership Tier
     */
    private PartnershipTier partnershipTier;

    /**
     * Payment Details
     */
    private Date paymentDate;

    private PaymentStatus paymentStatus;

    private String invoiceLink;

    /**
     * Internal assigned AE
     */
    private String assignedAe;

    /**
     * Added from UI/Image Requirements
     */

    // Recent Leads table status label
    private String statusLabel;

    // Action button label
    private String actionLabel;

    // Action redirect URL
    private String actionUrl;

    // Demo scheduled / onboarded etc timestamps
    private Date submittedDate;

    // Display commission range text
    private String estimatedCommissionDisplay;

    // Partner badge text
    private String tierDisplay;

    // UI flags
    private Boolean canResubmit;

    private Boolean canCancel;

    private Boolean canViewCommission;

    private Boolean canViewProgress;

    private Boolean canViewDetails;

    /**
     * Optional CRM / Deal Tracking
     */
    private String crmDealId;

    private String hubspotDealId;

}
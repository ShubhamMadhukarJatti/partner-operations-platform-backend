package com.sharkdom.partnerprogram.dtos;

import com.sharkdom.partnerprogram.enums.PartnershipTier;
import com.sharkdom.reseller.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerCommissionDTO {

    /**
     * Lead ID
     */
    private Long id;

    /**
     * Partner User ID
     */
    private String userId;

    /**
     * Company details
     */
    private String companyName;

    /**
     * Partner tier
     */
    private PartnershipTier partnershipTier;

    /**
     * Deal value (ACV)
     */
    private BigDecimal dealAcv;

    /**
     * Commission %
     */
    private BigDecimal rate;

    /**
     * Final commission amount
     */
    private BigDecimal commission;

    /**
     * Payment status
     */
    private PaymentStatus paymentStatus;

    /**
     * Payment date
     */
    private Date paymentDate;

    /**
     * Invoice URL
     */
    private String invoiceUrl;

    /**
     * Assigned AE
     */
    private String assignedAe;
}
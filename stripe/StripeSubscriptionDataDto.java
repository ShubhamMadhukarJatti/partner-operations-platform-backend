package com.sharkdom.model.stripe;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sharkdom.constants.stripe.StripeSubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripeSubscriptionDataDto {

    private Long id;

    private String subscriptionId;

    private Long trialPeriodDays;

    private Set<Long> organizationId;

    private StripeSubscriptionStatus status;

    private Long amount;

    private LocalDate endOn;

    private LocalDate startOn;

    private LocalDate cancelledOn;

    private String cancellationReason;

    private Integer runningMonth;

    private String additionalInfo;

    private String transactionId;

    private Long quantity;

    private Long seatLeft;

    private Long seatAssign;

    private String latestInvoice;

    @JsonBackReference
    private List<StripeCheckoutSessionsDto> checkoutSession;

    private PriceEntityDto price;

    private StripeCustomerDto customer;

}

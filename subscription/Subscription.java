package com.sharkdom.entity.subscription;

import com.sharkdom.constants.subscription.SubscriptionStatus;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "subscription", uniqueConstraints = {@UniqueConstraint(columnNames = {"transactionId"})})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String userId;
    private SubscriptionStatus status;
    private LocalDate endOn;
    private LocalDate startOn;
    private String planCode;
    private Date cancelledOn;
    private String cancellationReason;
    private Integer runningMonth;
    private long paymentFk;
    private String additionalInfo;
    private long organizationId;
    private Long amount;
    private String transactionId;
}

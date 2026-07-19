package com.sharkdom.subscription.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionPlanFreeTrailResponse {

    private Long id;

    private String userEmail;

    private Long numberOfSeats;

    private BigDecimal price;

    private String organizationName;

    private String country;

    private String address;

    private String gstInId;

}
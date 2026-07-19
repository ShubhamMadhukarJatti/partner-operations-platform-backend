package com.sharkdom.subscription.model;

import com.sharkdom.subscription.entity.ModuleName;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
public class ModuleSubscriptionPlanResponse {

    private Long id;
    private Long orgId;
    private Long numberOfSeats;
    private BigDecimal price;
    private String organizationName;
    private String country;
    private String address;
    private String gstInId;
    private String email;
}
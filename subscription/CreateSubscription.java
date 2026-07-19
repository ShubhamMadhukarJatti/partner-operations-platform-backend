package com.sharkdom.model.subscription;

import com.sharkdom.constants.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class CreateSubscription {
    private String organizationName;
    private Long organizationId;
    private PlanType planType;
    private String referralCode;
    private String email;
    private String contactNumber;
}

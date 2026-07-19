package com.sharkdom.subscription.model;

import lombok.Data;

@Data
public class UpdateModuleSubscriptionPlanRequest {
    private String organizationName;
    private String country;
    private String address;
    private String gstInId;
}

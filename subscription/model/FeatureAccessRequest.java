package com.sharkdom.subscription.model;

import com.sharkdom.subscription.entity.SuiteKey;
import lombok.Data;

@Data
public class FeatureAccessRequest {

    private SuiteKey suiteKey;
    private boolean freeFeature;
}
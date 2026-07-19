package com.sharkdom.gtm.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TargetType {
    @JsonProperty("Revenue ($)")
    REVENUE,

    @JsonProperty("Pipeline ($)")
    PIPELINE,

    @JsonProperty("Leads Generated")
    LEADS_GENERATED,

    @JsonProperty("Opportunities Registered")
    OPPORTUNITIES_REGISTERED,

    @JsonProperty("Customers Onboarded")
    CUSTOMERS_ONBOARDED,

    @JsonProperty("Active Integrations")
    ACTIVE_INTEGRATIONS,

    @JsonProperty("Campaigns Launched")
    CAMPAIGNS_LAUNCHED,

    @JsonProperty("Partner Engagements")
    PARTNER_ENGAGEMENTS,

    @JsonProperty("MQLs")
    MQLS,

    @JsonProperty("SQLs")
    SQLS
}

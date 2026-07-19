package com.sharkdom.salesforce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.service.organization.OrganizationService;

import java.util.List;
import java.util.Map;

public record SalesforceQueryResponse(
        @JsonProperty("totalSize") int totalSize,
        @JsonProperty("done") boolean done,
        @JsonProperty("records") List<Map<String, Object>> records
) {}



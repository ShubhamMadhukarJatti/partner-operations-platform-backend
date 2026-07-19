package com.sharkdom.deals.model;

public record DealsResponse(String dealId,
                            String organizationName,
                            String organizationType,
                            String organizationBrief,
                            String dealBrief,
                            String status,
                            Long organizationId,
                            String logoUrl) {
}

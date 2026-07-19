package com.sharkdom.deals.model;

public record CreateDealRequest(Long organizationId,
                                String offerDetail,
                                Integer commission,
                                String[] restrictedSectors,
                                String[] channelAllowed,
                                String quotaRemaining,
                                String geography,
                                boolean approvalRequired,
                                String status,
                                String pageURL) {
}

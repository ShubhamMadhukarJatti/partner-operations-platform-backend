package com.sharkdom.entity.ai;

public record OverlapRecordFieldResponse(
        String name,
        String companyName,
        String contactEmail,
        String domain,
        String dealStage,
        String creationDate,
        String closeDate,
        String subscribed,
        String ticketSize
) {}
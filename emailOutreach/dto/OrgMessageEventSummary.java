package com.sharkdom.emailOutreach.dto;

import lombok.Data;

@Data
public class OrgMessageEventSummary {
    private Long orgId;
    private Long totalEmails;
    private Long opened;
    private Long delivered;
    private Long bounced;
    private Long dropped;
    private Long complained;
    private Long unsubscribed;
    private Long accepted;
    private Long clicked;
    private Double engagementRate;
    private Double openRate;
    private Double clickRate;
}
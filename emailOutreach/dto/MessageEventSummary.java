package com.sharkdom.emailOutreach.dto;

import lombok.Data;

@Data
public class MessageEventSummary {
    private Long opened;
    private Long delivered;
    private Long bounced;
    private Long dropped;
    private Long complained;
    private Long unsubscribed;
    private Long accepted;
    private Long clicked;
}
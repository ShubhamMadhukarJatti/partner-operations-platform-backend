package com.sharkdom.agenticai.model;

import lombok.Data;

@Data
public class OutreachTransactionSummaryResponse {

    private Long orgId;

    private long emailCount;

    private long linkedinCount;

    private long totalTransactions;
}

package com.sharkdom.deals.model;

import com.sharkdom.constants.user.ApprovalRequestHistoryStatus;
import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class DealJoinerResponse {
    private Long id;
    private String dealId;
    private String userId;
    private Long organizationId;
    private ApprovalRequestHistoryStatus status;
    private String affiliateCode;
    private String organizationName;
    private String affiliateLink;
    private String testWebhookUrl;
    private String prodWebhookUrl;
}

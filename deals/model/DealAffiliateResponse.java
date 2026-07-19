package com.sharkdom.deals.model;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class DealAffiliateResponse {
    private String affiliateCode;
    private String affiliateLink;
    private String testWebhookUrl;
    private String prodWebhookUrl;
}

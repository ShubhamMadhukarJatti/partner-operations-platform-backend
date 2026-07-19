package com.sharkdom.model.stripe;

import lombok.Data;

@Data
public class StripeAccountLinkResponse {
    private String object;
    private Long created;
    private Long expires_at;
    private String url;
}

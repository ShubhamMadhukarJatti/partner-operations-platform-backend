package com.sharkdom.model.ai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HubspotTokenResponse {
    private String token_type;
    private String refresh_token;
    private String access_token;
    private int expires_in;
}

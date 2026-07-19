package com.sharkdom.model.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ZohoToken {
    private String access_token;
    private String scope;
    private String api_domain;
    private String token_type;
    private int expires_in;
}

package com.sharkdom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SetupIntentResponse {

    private String setupIntentId;
    private String clientSecret;

}

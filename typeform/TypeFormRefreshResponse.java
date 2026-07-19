package com.sharkdom.model.typeform;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypeFormRefreshResponse {

    private String accessToken;
    private String refreshToken;
}

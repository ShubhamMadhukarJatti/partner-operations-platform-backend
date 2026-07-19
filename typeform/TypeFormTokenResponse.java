package com.sharkdom.model.typeform;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypeFormTokenResponse {
    private String tokenType;
    private String accessToken;
    private Integer expiresIn;
    private String refreshToken;
}

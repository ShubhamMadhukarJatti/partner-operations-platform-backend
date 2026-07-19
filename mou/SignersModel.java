package com.sharkdom.model.mou;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class SignersModel {
    private String identifier;
    private String displayName;
    private SignatureModel signature;
}

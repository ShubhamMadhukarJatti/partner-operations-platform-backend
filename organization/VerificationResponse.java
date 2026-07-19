package com.sharkdom.model.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class VerificationResponse {
    boolean verified;
}

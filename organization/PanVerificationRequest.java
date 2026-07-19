package com.sharkdom.model.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class PanVerificationRequest {
    String pan;
    String consent;
    String reason;
}

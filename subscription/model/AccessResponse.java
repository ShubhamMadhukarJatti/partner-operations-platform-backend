package com.sharkdom.subscription.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccessResponse {
    private boolean allowed;
    private String reason;

    public static AccessResponse allowed(String reason) {
        return new AccessResponse(true, reason);
    }

    public static AccessResponse denied(String reason) {
        return new AccessResponse(false, reason);
    }
}

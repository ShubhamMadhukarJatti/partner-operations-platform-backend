package com.sharkdom.model.email;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailVerificationResponse {
    private boolean success;
    private String email;
    private String message;
    private String status;
}
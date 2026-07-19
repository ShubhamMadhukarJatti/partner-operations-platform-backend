package com.sharkdom.registration.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
public class RefreshTokenResponse {

    private Long id;
    private String token;
    private LocalDateTime expiryDate;
    private String userId;
    private String userEmail;
    private Date generatedAt;
}

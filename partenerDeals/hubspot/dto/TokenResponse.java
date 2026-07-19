package com.sharkdom.service.partenerDeals.hubspot.dto;

public class TokenResponse {

    private String accessToken;
    private String refreshToken;

    // Constructor
    public TokenResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // Getters
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
}

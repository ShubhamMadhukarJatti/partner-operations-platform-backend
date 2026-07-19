package com.sharkdom.registration.model;

public record LoginResponse(String accessToken, String refreshToken, String userId, Boolean isOnboarded) {
}

package com.sharkdom.registration.model;

import jakarta.validation.constraints.Email;

public record UserVerifyRequest(
        @Email(message = "SH04") String email,
        String otp

//        boolean isPartnerPortalUser

) {
}

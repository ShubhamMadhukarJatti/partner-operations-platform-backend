package com.sharkdom.registration.model;

import jakarta.validation.constraints.Email;

public record UserLoginRequest(
        @Email(message = "SH04") String email
) {
}

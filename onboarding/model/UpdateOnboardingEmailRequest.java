package com.sharkdom.onboarding.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOnboardingEmailRequest {

    @NotBlank(message = "companyURL is required")
    private String companyURL;

    @NotBlank(message = "email is required")
    @Email(message = "Invalid email format")
    private String email;
}

package com.sharkdom.partnerprogram.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PartnerPasswordLoginRequest {

    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}

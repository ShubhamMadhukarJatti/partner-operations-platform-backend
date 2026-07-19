package com.sharkdom.partnerprogram.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SetPasswordRequest {

    @NotBlank(message = "New password cannot be blank")
    private String newPassword;

    @NotBlank(message = "Retype password cannot be blank")
    private String retypePassword;
}

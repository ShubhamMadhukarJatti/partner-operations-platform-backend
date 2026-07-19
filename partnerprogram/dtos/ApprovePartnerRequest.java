package com.sharkdom.partnerprogram.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApprovePartnerRequest {

    @NotBlank
    @Email
    private String email;
}

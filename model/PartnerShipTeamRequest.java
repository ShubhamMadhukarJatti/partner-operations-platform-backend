package com.sharkdom.agenticai.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PartnerShipTeamRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String linkedin;

    @NotBlank
    private String title;

    private String email;
}
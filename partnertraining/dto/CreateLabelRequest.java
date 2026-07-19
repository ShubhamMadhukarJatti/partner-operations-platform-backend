package com.sharkdom.partnertraining.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateLabelRequest {

    @NotBlank
    private String name;

}

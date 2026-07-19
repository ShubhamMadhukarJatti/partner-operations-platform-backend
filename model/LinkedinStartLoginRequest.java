package com.sharkdom.agenticai.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LinkedinStartLoginRequest {

    @NotBlank(message = "userId must not be empty")
    private String userId;

    private boolean force;
}

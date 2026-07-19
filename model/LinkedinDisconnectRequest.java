package com.sharkdom.agenticai.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LinkedinDisconnectRequest {

    @NotBlank(message = "userId must not be empty")
    private String userId;
}

package com.sharkdom.agenticai.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendConnectionRequest {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "profileUrl is required")
    private String profileUrl;

    // Optional — empty means no message
    private String connectionMessage;
}
package com.sharkdom.agenticai.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LinkedinSendConnectionRequest {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "profileUrl is required")
    private String profileUrl;

    // Optional
    private String connectionMessage;
}
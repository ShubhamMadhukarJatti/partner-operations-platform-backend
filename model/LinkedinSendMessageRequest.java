package com.sharkdom.agenticai.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LinkedinSendMessageRequest {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "profileUrl is required")
    private String profileUrl;

    @NotBlank(message = "message is required")
    private String message;
}
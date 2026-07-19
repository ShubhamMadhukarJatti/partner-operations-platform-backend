package com.sharkdom.partnerattribution.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Schema(description = "Request DTO for sending or scheduling notifications")
public class NotificationRequestDTO {

    @Schema(description = "body of email")
    private String body;

    @Schema(description = "subject of email")
    private String subject;

    @Schema(description = "Receiver email")
    String receiverEmail;

    @Schema(description = "Receiver name")
    String receiverName;

    @Schema(description = "Set true if notification should be scheduled later", example = "true")
    private boolean scheduled;

    @Schema(description = "Time when notification should be sent (required if scheduled=true)",
            example = "2026-04-20T10:30:00")
    private LocalDateTime scheduleTime;

    @Schema(description = "Dynamic template data")
    private Map<String, Object> data;
}
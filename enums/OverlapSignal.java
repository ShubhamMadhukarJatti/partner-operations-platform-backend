package com.sharkdom.partnerattribution.enums;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OverlapSignal {

    private boolean active;

    private LocalDateTime detectedAt;
}

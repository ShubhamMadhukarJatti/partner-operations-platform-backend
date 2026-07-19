package com.sharkdom.partnerattribution.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnershipResponseMonitorDto {

    private String currentStage;

    private List<StageDto> stages;

    private ResponseMonitorDto responseMonitor;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageDto {

        private String stageName;

        private boolean completed;

        private boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseMonitorDto {

        private boolean emailDelivered;

        private boolean replyReceived;

        private boolean emailOpened;

        private String emailOpenedAt;

        private String responseDeadline;

        private String timeRemaining;

        private String lastChecked;
    }
}
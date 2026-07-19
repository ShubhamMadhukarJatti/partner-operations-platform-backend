package com.sharkdom.profilesection.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileCompletionStatusResponse {

    private boolean partnerProgramPublished;
    private boolean dataSourceConnected;
    private boolean profileCompleted;

    private int completionPercentage;
}
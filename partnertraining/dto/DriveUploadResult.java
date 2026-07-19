package com.sharkdom.partnertraining.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DriveUploadResult {
    private String fileName;
    private long fileSizeBytes;
    private String gcpUrl;
}


package com.sharkdom.partnertraining.dto;

import lombok.Data;

@Data
public class CoverImageUploadResponseDto {
    private String fileUrl;

    public CoverImageUploadResponseDto(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}

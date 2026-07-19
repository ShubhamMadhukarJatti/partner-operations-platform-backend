package com.sharkdom.partnerattribution.dto;

import lombok.Data;

@Data
public class FileUploadResponseDto {

    private String fileUrl;

    public FileUploadResponseDto(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}

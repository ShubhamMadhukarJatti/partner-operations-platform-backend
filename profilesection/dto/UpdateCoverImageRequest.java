package com.sharkdom.profilesection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCoverImageRequest {

    @NotBlank(message = "Cover image URL is required")
    private String coverImageUrl;
}

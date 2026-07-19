package com.sharkdom.profilesection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrganizationResourceRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String type;

    @NotBlank
    private String source;

    @NotBlank
    private String url;

}
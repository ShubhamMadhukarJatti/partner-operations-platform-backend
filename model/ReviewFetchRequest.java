package com.sharkdom.agenticai.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewFetchRequest {

    @NotBlank(message = "orgName is required")
    private String orgName;

    @NotBlank(message = "orgWebURL is required")
    private String orgWebURL;

}
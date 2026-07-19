package com.sharkdom.model.ppi;

import lombok.Data;

@Data
public class CreateVersionRequest {
    private String description;
    private String scriptId;
    private String accessToken;

}
